package com.atguigu.gmall.wms.vo;

import lombok.Data;

@Data
public class SkuLockVO {
    private Long skuId;
    private Integer count;
    private Boolean lock;
    private Long wareSkuId;//锁定库存的id
    private String orderToken;
}
