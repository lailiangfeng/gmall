package com.atguigu.gmall.ums.dao;

import com.atguigu.gmall.ums.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author llf
 * @email llf@atguigu.com
 * @date 2020-03-08 11:42:48
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
