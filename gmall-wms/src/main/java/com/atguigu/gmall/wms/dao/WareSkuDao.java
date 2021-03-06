package com.atguigu.gmall.wms.dao;

import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 * 
 * @author llf
 * @email llf@atguigu.com
 * @date 2020-06-08 16:27:27
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    List<WareSkuEntity> checkStore(@Param("skuId") Long skuId, @Param("count") Integer count);
//多个参数需要家@parm注解
    int lockStore(@Param("id") Long id, @Param("count") Integer count);

    int unLockStore(@Param("wareSkuId") Long wareSkuId,@Param("count") Integer count);

    int minusStore(Long wareSkuId, Integer count);
}
