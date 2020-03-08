package com.atguigu.gmall.sms.dao;

import com.atguigu.gmall.sms.entity.SpuLadderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品阶梯价格
 * 
 * @author llf
 * @email llf@atguigu.com
 * @date 2020-03-08 11:39:31
 */
@Mapper
public interface SpuLadderDao extends BaseMapper<SpuLadderEntity> {
	
}
