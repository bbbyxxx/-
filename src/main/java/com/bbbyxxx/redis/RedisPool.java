package com.bbbyxxx.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Component
public class RedisPool {
    @Autowired
    RedisConfig redisConfig;

    @Bean//加在方法上，此方法返回的对象被IOC容器管理
    public JedisPool jedisPoolFactory(){
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxIdle(redisConfig.getPoolMaxIdle());
        poolConfig.setMaxTotal(redisConfig.getPoolMaxTotal());
        poolConfig.setMaxWaitMillis(redisConfig.getPoolMaxWait()*1000);
        JedisPool jp = new JedisPool(poolConfig,redisConfig.getHost(),
                redisConfig.getPort(),redisConfig.getTimeout()*1000,redisConfig.getPassword(),0);
        return jp;
    }
}
