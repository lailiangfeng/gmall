package com.atguigu.gmall.sms.vo;

import lombok.Data;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
@Data
public class SkuSaleVo {
    private Long SkuId;
    //积分营销相关字段
    private BigDecimal growBounds;
    private BigDecimal buyBounds;
    private List<Integer> work;

    //打折相关字段
    private Integer fullCount;
    private BigDecimal discount;
    private Integer ladderAddOther;
    //满减相关字段
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private Integer fullAddOther;
}
