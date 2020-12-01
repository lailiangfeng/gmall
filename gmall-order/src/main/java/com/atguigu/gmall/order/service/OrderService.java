package com.atguigu.gmall.order.service;

import com.atguigu.core.bean.Resp;
import com.atguigu.core.bean.UserInfo;
import com.atguigu.core.exception.OrderException;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.order.feign.*;
import com.atguigu.gmall.order.interceptor.LoginInterceptor;
import com.atguigu.gmall.order.vo.OrderConfimVO;
import com.atguigu.gmall.oms.vo.OrderItemVO;
import com.atguigu.gmall.oms.vo.OrderSubmitVO;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.ums.entity.MemberEntity;
import com.atguigu.gmall.ums.entity.MemberReceiveAddressEntity;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.vo.SkuLockVO;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class OrderService {
    @Autowired
    private GmallUmsClient umsClient;

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private GmallCartClient cartClient;

    @Autowired
    private GmallSmsClient smsClient;

    @Autowired
    private GmallOmsClient omsClient;

    @Autowired
    private StringRedisTemplate redisTemplate;
    private static final String TOKEN_PREFIX = "order:token:";

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;
    @Autowired
    private AmqpTemplate amqpTemplate;

    public OrderConfimVO confirm() {
        OrderConfimVO orderConfimVO = new OrderConfimVO();

        UserInfo userInfo = LoginInterceptor.getUserInfo();
        Long userId = userInfo.getId();
        if (userId == null){
            return null;
        }

        CompletableFuture<Void> addressCompletableFuture = CompletableFuture.runAsync(()->{
            //获取用户的收获地址 通过userid查找
            Resp<List<MemberReceiveAddressEntity>> addressesResp = this.umsClient.queryAddressesByUserId(userId);
            List<MemberReceiveAddressEntity> memberReceiveAddressEnties = addressesResp.getData();
            orderConfimVO.setAddresses(memberReceiveAddressEnties);
        },threadPoolExecutor);
        //获取购物车中选中的商品信息
        CompletableFuture<Void> bigskuCompletableFuture = CompletableFuture.supplyAsync(() -> {
            Resp<List<Cart>> cartsResp = this.cartClient.queryCheckedCartsByUserId(userId);
            List<Cart> cartList = cartsResp.getData();
            if (CollectionUtils.isEmpty(cartList)) {
                throw new OrderException("请勾选购物车商品");
            }
            return cartList;
        }, threadPoolExecutor).thenAcceptAsync(cartList -> {
            List<OrderItemVO> itemVOS = cartList.stream().map(cart -> {
                OrderItemVO orderItemVO = new OrderItemVO();
                Long skuId = cart.getSkuId();
                CompletableFuture<Void> skuCompletableFuture = CompletableFuture.runAsync(() -> {
                    Resp<SkuInfoEntity> skuInfoEntityResp = this.pmsClient.querySkuById(skuId);
                    SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
                    if (skuInfoEntity != null) {
                        orderItemVO.setSkuId(skuId);
                        orderItemVO.setWeight(skuInfoEntity.getWeight());
                        orderItemVO.setCount(cart.getCount());
                        orderItemVO.setDefaultImage(skuInfoEntity.getSkuDefaultImg());
                        orderItemVO.setPrice(skuInfoEntity.getPrice());
                        orderItemVO.setTitle(skuInfoEntity.getSkuTitle());
                    }
                }, threadPoolExecutor);

                CompletableFuture<Void> saleAttrCompletableFuture = CompletableFuture.runAsync(() -> {
                    Resp<List<SkuSaleAttrValueEntity>> saleAttrValueResp = pmsClient.querySkuSaleAttrValuesBySkuId(skuId);
                    List<SkuSaleAttrValueEntity> attrValueEnties = saleAttrValueResp.getData();
                    orderItemVO.setSaleAttrValues(attrValueEnties);
                }, threadPoolExecutor);

                CompletableFuture<Void> wareSkuCompletableFuture = CompletableFuture.runAsync(() -> {
                    Resp<List<WareSkuEntity>> wareSkuResp = this.wmsClient.queryWareSkusBySkuId(skuId);
                    List<WareSkuEntity> wareSkuEntities = wareSkuResp.getData();
                    if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                        orderItemVO.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0));
                    }
                }, threadPoolExecutor);
                CompletableFuture.allOf(skuCompletableFuture, saleAttrCompletableFuture, wareSkuCompletableFuture).join();
                return orderItemVO;
            }).collect(Collectors.toList());
            orderConfimVO.setOrderItems(itemVOS);
        }, threadPoolExecutor);


        //查询用户信息获取积分
        CompletableFuture<Void> memberCompletableFuture = CompletableFuture.runAsync(() -> {
            Resp<MemberEntity> memberEntityResp = this.umsClient.queryMemberById(userId);
            MemberEntity memberEntity = memberEntityResp.getData();
            orderConfimVO.setBounds(memberEntity.getIntegration());
        }, threadPoolExecutor);

        //生成唯一标示防止订单重复提交
        CompletableFuture<Void> orderTokenCompletableFuture = CompletableFuture.runAsync(() -> {
            String orderToken = IdWorker.getIdStr();
            orderConfimVO.setOrderToken(orderToken);
            this.redisTemplate.opsForValue().set(TOKEN_PREFIX+orderToken,orderToken);
        }, threadPoolExecutor);


        CompletableFuture.allOf(addressCompletableFuture,bigskuCompletableFuture,memberCompletableFuture,orderTokenCompletableFuture).join();
        return orderConfimVO;
    }

    public OrderEntity submit(OrderSubmitVO submitVO) {
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        //        1. 验证令牌防止重复提交
        String orderToken = submitVO.getOrderToken();
        //lua脚本 保证原子性
        String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        //执行脚本
        Long flag = this.redisTemplate.execute(new DefaultRedisScript<>(script,Long.class), Arrays.asList(TOKEN_PREFIX + orderToken), orderToken);
        if(flag ==0){
          throw new OrderException("订单不可重复提交！");
        }
        //        2. 验证价格
        List<OrderItemVO> items = submitVO.getItems();
        BigDecimal totalPrice = submitVO.getTotalPrice();
        if(CollectionUtils.isEmpty(items)){
            throw new OrderException("没有购买的商品，请先到购物车中勾选商品");
        }
        //获取实时总价
        BigDecimal currentTotalPrice = items.stream().map(item -> {
            Resp<SkuInfoEntity> skuInfoEntityResp = this.pmsClient.querySkuById(item.getSkuId());
            SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
            if (skuInfoEntity != null) {
                return skuInfoEntity.getPrice().multiply(new BigDecimal(item.getCount()));
            } else {
                return new BigDecimal(0);
            }
        }).reduce((a, b) -> a.add(b)).get();
        if(currentTotalPrice.compareTo(totalPrice)!=0){
            throw new OrderException("页面价格已经过期，请刷新");
        }

        //        3. 验证库存，并锁定库存
        List<SkuLockVO> lockVOS = items.stream().map(orderItemVO->{
            SkuLockVO skuLockVO = new SkuLockVO();
            skuLockVO.setSkuId(orderItemVO.getSkuId());
            skuLockVO.setCount(orderItemVO.getCount());
            skuLockVO.setOrderToken(orderToken);
            return skuLockVO;
        }).collect(Collectors.toList());
        Resp<Object> wareResp = wmsClient.checkAndLockStore(lockVOS);
        if(wareResp.getCode()!=0){
            throw new OrderException(wareResp.getMsg());
        }

        //        4. 生成订单
        Resp<OrderEntity> orderEntityResp = null;
        try {
            submitVO.setUserId(userInfo.getId());
            orderEntityResp = this.omsClient.saveOrder(submitVO);
        }catch (Exception e){
            e.printStackTrace();
            //发送消息给wms解锁对应的库存
            this.amqpTemplate.convertAndSend("GMALL-ORDER-EXCHANGE","stock.unlock",orderToken);
            throw new OrderException("服务器错误，创建订单失败");
        }
        //        5. 删购物车中对应的记录（消息队列）
        Map<String,Object> map = new HashMap<>();
        map.put("userId",userInfo.getId());
//        List<Object> skuIds = items.stream().map(orderItemVO -> {
//                    return orderItemVO.getSkuId();
//                }).collect(Collectors.toList());
        List<Object> skuIds = items.stream().map(OrderItemVO::getSkuId).collect(Collectors.toList());
        map.put("skuId",skuIds);
        this.amqpTemplate.convertAndSend("GMALL-ORDER-EXCHANGE","cart.delete",map);
        if (orderEntityResp != null){
            return orderEntityResp.getData();
        }
        return null;
    }
    
    
    
    //定时任务
    public static void main(String[] args) {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(()->{
            System.out.printf("这是一个定时任务");
        },10l,20l, TimeUnit.SECONDS);
    }
}
