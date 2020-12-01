package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.vo.SpuInfoVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.pms.entity.SpuInfoEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * spu信息
 *
 * @author llf
 * @email llf@atguigu.com
 * @date 2020-03-08 11:29:19
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

     PageVo querySpuPage(QueryCondition condition, Long cid);


    PageVo queryPage(QueryCondition params);

    void bigSave(SpuInfoVo spuInfoVo);

    String save222(String id);


    // public SpuInfoVo getById1(Long id);

}

