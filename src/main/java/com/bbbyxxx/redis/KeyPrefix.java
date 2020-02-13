package com.bbbyxxx.redis;

public interface KeyPrefix {
    public int expireSeconds();//过期时间
    public String getPrefix();//获取前缀
}
