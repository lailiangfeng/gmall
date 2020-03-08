package com.atguigu.gmall.oms.dao;

import com.atguigu.gmall.oms.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author llf
 * @email llf@atguigu.com
 * @date 2020-03-08 11:35:40
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
