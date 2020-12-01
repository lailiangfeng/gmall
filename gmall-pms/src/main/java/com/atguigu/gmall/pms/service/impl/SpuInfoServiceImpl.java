package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.dao.ProductAttrValueDao;
import com.atguigu.gmall.pms.dao.SkuInfoDao;
import com.atguigu.gmall.pms.dao.SpuInfoDescDao;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.feign.GmallSmsClient;
import com.atguigu.gmall.pms.service.SkuImagesService;
import com.atguigu.gmall.pms.service.SkuSaleAttrValueService;
import com.atguigu.gmall.pms.vo.BaseAttrVo;
import com.atguigu.gmall.pms.vo.SkuInfoVO;
import com.atguigu.gmall.pms.vo.SpuInfoVo;
//import com.atguigu.gmall.sms.vo.SkuSaleVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.pms.dao.SpuInfoDao;
import com.atguigu.gmall.pms.service.SpuInfoService;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoDescDao spuInfoDescDao;
    @Autowired
    private ProductAttrValueDao productAttrValueDao;
    @Autowired
    private ProductAttrValueServiceImpl productAttrValueService;
    @Autowired
    private SkuInfoDao skuInfoDao;
    @Autowired
    private SkuImagesService skuImagesService;
    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    private GmallSmsClient gmallSmsClient;
    @Autowired
    private SpuInfoDescServiceImpl spuInfoDescService;
    @Autowired
    private AmqpTemplate amqpTemplate;
    @Value("${item.rabbitmq.exchange}")
    private String EXCHANGE_NAME;
  //  @Autowired
   // private SkuSaleVo skuSaleVo;


    @Override
    public PageVo querySpuPage(QueryCondition condition, Long cid) {
        QueryWrapper wrapper = new QueryWrapper<SpuInfoEntity>();
        //判断分类是否为0
        if(cid != 0){
            wrapper.eq("catalog_id",cid);
        }
        //判断关键字是否为空
        String key = condition.getKey();
       // System.out.println(key);
        if(StringUtils.isNoneBlank(key)){
            //wrapper.and(t ->t.eq("id",key).or().like("spu_name",key));
        }
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(condition),
                wrapper
        );

        return new PageVo(page);
    }

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageVo(page);
    }

    @Override
    @GlobalTransactional
    public void bigSave(SpuInfoVo spuInfoVo) {
        //1.保存spu相关的三张表
            //1.1保存pms_spu_info
        Long spuId = saveSpuInfo(spuInfoVo);
        //1.2保存pms_spu_info_desc
        spuInfoDescService.saveSpuInfoDesc(spuInfoVo, spuId);
        //1.3保存pms_product_attr_value
        saveBaseAttrValue(spuInfoVo, spuId);

        //2.保存sku相关的三张表
        saveSkuAndSale(spuInfoVo, spuId);
        //int i=1/0;
        sendMsg("insert",spuId);

    }

    private void sendMsg(String type,Long spuId){
        this.amqpTemplate.convertAndSend(EXCHANGE_NAME,"item"+type,spuId);

    }

    private void saveSkuAndSale(SpuInfoVo spuInfoVo, Long spuId) {
        List<SkuInfoVO> skus = spuInfoVo.getSkus();
        if (CollectionUtils.isEmpty(skus)){
            return;
        }
        skus.forEach(skuInfoVO -> {
            //2.1保存pms_sku_info
            skuInfoVO.setSpuId(spuId);
            skuInfoVO.setSkuCode(UUID.randomUUID().toString());
            skuInfoVO.setBrandId(spuInfoVo.getBrandId());
            skuInfoVO.setCatalogId(spuInfoVo.getCatalogId());
            List<String> images = skuInfoVO.getImages();
            if (!CollectionUtils.isEmpty(images)){
                skuInfoVO.setSkuDefaultImg(StringUtils.isNotBlank(skuInfoVO.getSkuDefaultImg()) ? skuInfoVO.getSkuDefaultImg() : images.get(0));
            }
            this.skuInfoDao.insert(skuInfoVO);
            Long skuId = skuInfoVO.getSkuId();
            //2.2保存pms_sku_images
            if (!CollectionUtils.isEmpty(images)){
                List<SkuImagesEntity> skuImagesEntities = images.stream().map(
                        image -> {
                            SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                            skuImagesEntity.setSkuId(skuId);
                            skuImagesEntity.setImgUrl(image);
                            //判断是否默认图片
                            skuImagesEntity.setDefaultImg(StringUtils.equals(skuInfoVO.getSkuDefaultImg(), image) ? 1 : 0);
                            return skuImagesEntity;
                        }
                ).collect(Collectors.toList());
                this.skuImagesService.saveBatch(skuImagesEntities);
            }
            //2.3保存pms_sale_attr_value
            List<SkuSaleAttrValueEntity> saleAttrs = skuInfoVO.getSaleAttrs();
            if (!CollectionUtils.isEmpty(saleAttrs)){
                //设置skuid
                saleAttrs.forEach(skuSaleAttrValueEntity -> skuSaleAttrValueEntity.setSkuId(skuId));
                //批量保存
                this.skuSaleAttrValueService.saveBatch(saleAttrs);
            }

            //3.保存营销信息的相关的三张表（以下三部为feign远程调用sms的服务）
            //3.1保存sms_sku_bounds
            //3.2保存sms_sku_ladder
            //3.3保存sms_sku_full_reduction
            SkuSaleVo skuSaleVo = new SkuSaleVo();
            BeanUtils.copyProperties(skuInfoVO,skuSaleVo);
            skuSaleVo.setSkuId(skuId);
            this.gmallSmsClient.saveSale(skuSaleVo);



        });
    }

    private void saveBaseAttrValue(SpuInfoVo spuInfoVo, Long spuId) {
        List<BaseAttrVo> baseAttrs = spuInfoVo.getBaseAttrs();
        if(!CollectionUtils.isEmpty(baseAttrs)){
            List<ProductAttrValueEntity> attrValueEntities = baseAttrs.stream().map(
                    baseAttrVo -> {
                        ProductAttrValueEntity attrValueEntity = baseAttrVo;
                        attrValueEntity.setSpuId(spuId);
                        return attrValueEntity;
                    }).collect(Collectors.toList());
            log.trace(attrValueEntities.toString());
            productAttrValueService.saveBatch(attrValueEntities);
        }
    }

    private Long saveSpuInfo(SpuInfoVo spuInfoVo) {
        spuInfoVo.setCreateTime(new Date());
        spuInfoVo.setUodateTime(spuInfoVo.getCreateTime());
        this.save(spuInfoVo);
        return spuInfoVo.getId();
    }

    @Override
    public String save222(String id) {
        return this.gmallSmsClient.saveSale222(id);
    }


//    @Override
//    public SpuInfoVo getById1(Integer user_id) {
//        return null;
//    }

}