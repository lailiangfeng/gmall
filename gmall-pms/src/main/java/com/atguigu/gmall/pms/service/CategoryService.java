package com.atguigu.gmall.pms.service;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.vo.CategoryVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;

import java.util.List;


/**
 * 商品三级分类
 *
 * @author llf
 * @email llf@atguigu.com
 * @date 2020-03-08 11:29:19
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageVo queryPage(QueryCondition params);

    List<CategoryVO>querysubsCategories(Long pid);
}

