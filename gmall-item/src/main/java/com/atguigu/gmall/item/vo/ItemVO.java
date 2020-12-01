package com.atguigu.gmall.item.vo;

import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.SkuImagesEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.pms.vo.ItemGroupVO;
import com.atguigu.gmall.sms.vo.SaleVO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ItemVO {
    private Long skuId;
    private CategoryEntity categoryEntity;
    private BrandEntity brandEntity;
    private Long spuId;
    private String spuName;

    private String skuTitle;
    private String subtitle;
    private BigDecimal price;
    private BigDecimal weight;

    //2、sku的所有图片
    private List<SkuImagesEntity> pics;

    //3、sku的所有促销信息
    private List<SaleVO> sales;
    private Boolean store;

    //4、sku的所有销售属性组合
    private List<SkuSaleAttrValueEntity> saleAttrs;
    private List<String> images;
    private List<ItemGroupVO> groups;


//    //5、spu的所有基本属性
//    private List<BaseGroupVO> attrGroups;
//
//    //6、详情介绍
//    private List<String> descs;

}
