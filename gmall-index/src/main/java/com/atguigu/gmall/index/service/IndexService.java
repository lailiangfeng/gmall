package com.atguigu.gmall.index.service;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.vo.CategoryVO;

import java.util.List;

public interface IndexService  {
    List<CategoryEntity> queryLv1Categories();

    List<CategoryVO> querysubsCategories(Long pid);
}
