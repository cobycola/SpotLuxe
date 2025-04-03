package com.SpotLuxe.common.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class RedisIdWorker {

    private static final long BEGIN_TIMESTAMP=1743334850L;
    private static final long COUNT_BITS=32L;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    public long nextId(String keyPrefix){
        //生成时间戳
        LocalDateTime now = LocalDateTime.now();
        long nowTime = now.toEpochSecond(ZoneOffset.UTC);
        long timestamp=nowTime-BEGIN_TIMESTAMP;
        //生成序列号

        //获取当前日期，精确到天
        String date = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String key="icr:"+keyPrefix+":"+date;
        //自增长
        long count = stringRedisTemplate.opsForValue().increment(key);

        //拼接并返回
        return timestamp << COUNT_BITS | count;
    }

}
