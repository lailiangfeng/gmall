package com.atguigu.gmall.oms.vo;

import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.sms.vo.SaleVO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
@Data
public class OrderItemVO {
    private Long skuId;// 商品id
    private String title;// 标题
    private String defaultImage;// 图片
    private BigDecimal price;// 价格
    private Boolean store;//是否有货
    private Integer count;// 购买数量
    private List<SkuSaleAttrValueEntity> saleAttrValues;// 商品规格参数
    private List<SaleVO> sales;
    private BigDecimal weight;
}
