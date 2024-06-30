package com.fjut.library_management_system.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;

import java.util.concurrent.TimeUnit;

@Component
@Order(-1)
//Redis工具类
public  class RedisUtil {
    //清除前缀为prefix的缓存
    public void removeCacheByPrefix(String prefix){
        //获取RedisTemplate
        RedisTemplate<String,Object> redisTemplate= (RedisTemplate<String,Object>) SpringContextUtil.getBean("stringRedisTemplate");
        //获取缓存
        Set<String> keys = redisTemplate.keys(prefix + "*");
        if (keys != null && !keys.isEmpty()) {
            //创建一个新的集合，用于存储需要删除的键
            Set<String> keysToDelete = new HashSet<>();
            for (String key : keys) {
                //如果键不等于"login:3221311414"，则添加到需要删除的键的集合中(管理员不能被删除)
                if (!key.equals("login:3221311414")) {
                    keysToDelete.add(key);
                }
            }
            //删除缓存
            if (!keysToDelete.isEmpty()) {
                redisTemplate.delete(keysToDelete);
            }
        }
    }
}

