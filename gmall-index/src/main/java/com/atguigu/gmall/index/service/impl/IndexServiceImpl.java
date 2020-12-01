package com.atguigu.gmall.index.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.index.annotation.GmallCache;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.vo.CategoryVO;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
public class IndexServiceImpl implements IndexService {
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private GmallPmsClient gmallPmsClient;
    @Autowired
    private StringRedisTemplate redisTemplate;
    private static final String KEY_PREFIX="index:cates:";
    @Override
    public List<CategoryEntity> queryLv1Categories() {
        Resp<List<CategoryEntity>> listResp = this.gmallPmsClient.queryCategoryByPidOrLevel(1, null);
        return listResp.getData();
    }

    @Override
    @GmallCache(prefix = "index:cates:",timeout = 7200,random = 100)
    public List<CategoryVO> querysubsCategories(Long pid) {
        //如果有缓存就取缓存的
//        String cateJson = this.redisTemplate.opsForValue().get(KEY_PREFIX + pid);
//        if (!StringUtils.isEmpty(cateJson)){
//            return JSON.parseArray(cateJson,CategoryVO.class);
//        }
        //加分布式锁
//        RLock lock = this.redissonClient.getLock("lock" + pid);
//        lock.lock();
//        String cateJson2 = this.redisTemplate.opsForValue().get(KEY_PREFIX + pid);
//        if (!StringUtils.isEmpty(cateJson2)){
//            //解锁
//            lock.unlock();
//            return JSON.parseArray(cateJson2,CategoryVO.class);
//        }

        //没有的画就写入缓存
        Resp<List<CategoryVO>> categories = this.gmallPmsClient.querysubsCategories(pid);
        List<CategoryVO> categoryVOS = categories.getData();
      //  this.redisTemplate.opsForValue().set(KEY_PREFIX + pid,JSON.toJSONString(categoryVOS));
        //解锁
        //lock.unlock();
        return categories.getData();
    }
}