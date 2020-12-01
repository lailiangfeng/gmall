package com.atguigu.gmall.cart.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.cart.feign.GmallPmsClient;
import com.atguigu.gmall.cart.feign.GmallSmsClient;
import com.atguigu.gmall.cart.feign.GmallWmsClient;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.cart.pojo.UserInfo;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.sms.vo.SaleVO;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class CartService {

    private static final String KEY_PREFIX = "gmall:cart:";
    private static final String PRICE_PREFIX = "gmall:sku:";

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private GmallPmsClient pmsClient;
    @Autowired
    private GmallWmsClient wmsClient;
    @Autowired
    private GmallSmsClient smsClient;

    public void addCart(Cart cart) {

        String key = KEY_PREFIX;
        //获取登入状态
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        if(userInfo.getId() != null ){
            key=key+userInfo.getId();
        }else {
            key+=userInfo.getUserKey();
        }

        //完成新增购物车逻辑
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);

        String skuId = cart.getSkuId().toString();
        Integer count = cart.getCount();
        //判断sku是否已经在购物车中  在的化就只需要增加数量
        if(hashOps.hasKey(skuId)){
            String cartJson = hashOps.get(skuId).toString();
            cart = JSON.parseObject(cartJson, Cart.class);
            cart.setCount(count+cart.getCount());

        }else{
            cart.setCheck(true);
            Resp<SkuInfoEntity> skuInfoEntityResp = this.pmsClient.querySkuById(cart.getSkuId());
            SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
            if (skuInfoEntity == null){
                return ;
            }
            cart.setDefaultImage(skuInfoEntity.getSkuDefaultImg());
            cart.setPrice(skuInfoEntity.getPrice());
            cart.setTitle(skuInfoEntity.getSkuTitle());

            Resp<List<SkuSaleAttrValueEntity>> listResp = this.pmsClient.querySkuSaleAttrValuesBySkuId(cart.getSkuId());
            List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = listResp.getData();
            cart.setSaleAttrValues(skuSaleAttrValueEntities);

            Resp<List<SaleVO>> saleResp = this.smsClient.querySalesBySkuId(cart.getSkuId());
            List<SaleVO> saleVOS = saleResp.getData();
            cart.setSales(saleVOS);

            Resp<List<WareSkuEntity>> wareResp = this.wmsClient.queryWareSkusBySkuId(cart.getSkuId());
            List<WareSkuEntity> wareSkuEntities = wareResp.getData();
            if(!CollectionUtils.isEmpty(wareSkuEntities)){
                cart.setStore(wareSkuEntities.stream().allMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0));
            }

            this.redisTemplate.opsForValue().set(PRICE_PREFIX+skuId,skuInfoEntity.getPrice().toString());

        }
        hashOps.put(skuId,JSON.toJSONString(cart));

    }

    public List<Cart> queryCarts() {
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        //查询未登入的购物车
        List<Cart> userKeyCarts = null;
        String userKey = KEY_PREFIX+ userInfo.getUserKey();
        BoundHashOperations<String, Object, Object> userKeyOps = this.redisTemplate.boundHashOps(userKey);
        List<Object> cartJsonList = userKeyOps.values();
        if (!CollectionUtils.isEmpty(cartJsonList)){
            userKeyCarts = cartJsonList.stream().map(cartJson -> {
                Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
                //查询当前价格
                String priceString = this.redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId());
                cart.setCurrentPrice(new BigDecimal(priceString));
                return cart;
            }).collect(Collectors.toList());
        }
        // 判断用户是否登录，未登录直接返回
        if (userInfo.getId() == null) {
            return userKeyCarts;
        }
        // 用户已登录，查询登录状态的购物车
        String key = KEY_PREFIX + userInfo.getId();
        BoundHashOperations<String, Object, Object> userIdOps = this.redisTemplate.boundHashOps(key); // 获取登录状态的购物车
        // 如果未登录状态的购物车不为空，需要合并
        if (!CollectionUtils.isEmpty(userKeyCarts)) {
            // 合并购物车
            userKeyCarts.forEach(userKeyCart -> {
                Long skuId = userKeyCart.getSkuId();
                Integer count = userKeyCart.getCount();
                if (userIdOps.hasKey(skuId.toString())) {
                    // 购物车已存在该记录，更新数量
                    String cartJson = userIdOps.get(skuId.toString()).toString();
                    userKeyCart = JSON.parseObject(cartJson, Cart.class);
                    userKeyCart.setCount(userKeyCart.getCount() + count);
                }
                // 购物车不存在该记录，新增记录
                userIdOps.put(skuId.toString(), JSON.toJSONString(userKeyCart));
            });
            // 合并完成后，删除未登录的购物车
            this.redisTemplate.delete(userKey);
        }
        // 返回登录状态的购物车
        List<Object> userCartJsonList = userIdOps.values();
        if (!CollectionUtils.isEmpty(userCartJsonList)) {
            return userCartJsonList.stream().map(userCartJson ->{
                Cart cart = JSON.parseObject(userCartJson.toString(), Cart.class);
                //查询当前价格
                String priceString = this.redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId());
                cart.setCurrentPrice(new BigDecimal(priceString));
                return cart;
            }).collect(Collectors.toList());
        }
        return null;

    }

    public void updateCart(Cart cart) {

        // 获取登陆信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();

        // 获取redis的key
        String key = KEY_PREFIX;
        if (userInfo.getId() == null) {
            key += userInfo.getUserKey();
        } else {
            key += userInfo.getId();
        }

        // 获取hash操作对象
        BoundHashOperations<String, Object, Object> hashOperations = this.redisTemplate.boundHashOps(key);
        String skuId = cart.getSkuId().toString();
        if (hashOperations.hasKey(skuId)) {
            // 获取购物车信息
            String cartJson = hashOperations.get(skuId).toString();
            Integer count = cart.getCount();
            cart = JSON.parseObject(cartJson, Cart.class);
            // 更新数量
            cart.setCount(count);
            // 写入购物车
            hashOperations.put(cart.getSkuId().toString(), JSON.toJSONString(cart));
        }else{
            System.out.println("222");
        }
    }

    public void deleteCart(Long skuId) {
        // 获取登陆信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();

        // 获取redis的key
        String key = KEY_PREFIX;
        if (userInfo.getId() == null) {
            key += userInfo.getUserKey();
        } else {
            key += userInfo.getId();
        }
        BoundHashOperations<String, Object, Object> hashOperations = this.redisTemplate.boundHashOps(key);
        hashOperations.delete(skuId.toString());
    }

    public List<Cart> queryCheckedCartsByUserId(Long userId) {
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);
        List<Object> cartJsonList = hashOps.values();
        return cartJsonList.stream()
                .map(cartJson -> JSON.parseObject(cartJson.toString(),Cart.class))
                .filter(Cart::getCheck)
                .collect(Collectors.toList());

    }
}
