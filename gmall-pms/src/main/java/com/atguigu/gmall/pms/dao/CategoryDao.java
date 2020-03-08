package com.atguigu.gmall.pms.dao;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author llf
 * @email llf@atguigu.com
 * @date 2020-03-08 11:29:19
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
