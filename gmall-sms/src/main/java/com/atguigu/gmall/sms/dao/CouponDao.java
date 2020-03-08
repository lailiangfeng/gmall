package com.atguigu.gmall.sms.dao;

import com.atguigu.gmall.sms.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author llf
 * @email llf@atguigu.com
 * @date 2020-03-08 11:39:30
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
