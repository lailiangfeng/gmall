package com.atguigu.gmall.pms.dao;

import com.atguigu.gmall.pms.entity.BrandEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 品牌
 * 
 * @author llf
 * @email llf@atguigu.com
 * @date 2020-03-08 11:29:19
 */
@Mapper
public interface BrandDao extends BaseMapper<BrandEntity> {

	//List<BrandEntity> selectBrand(@Param("id") Integer id);
}
