package com.kakuiwong.rxathanos.core.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class RxaRedisPub {

    @Autowired
    private StringRedisTemplate redisTemplate;

    public void pub(String channel, Object message) {
        redisTemplate.convertAndSend(channel, message);
    }
}
