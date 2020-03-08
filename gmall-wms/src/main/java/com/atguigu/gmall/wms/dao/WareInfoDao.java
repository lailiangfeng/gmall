package com.atguigu.gmall.wms.dao;

import com.atguigu.gmall.wms.entity.WareInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 仓库信息
 * 
 * @author llf
 * @email llf@atguigu.com
 * @date 2020-03-08 11:45:50
 */
@Mapper
public interface WareInfoDao extends BaseMapper<WareInfoEntity> {
	
}
