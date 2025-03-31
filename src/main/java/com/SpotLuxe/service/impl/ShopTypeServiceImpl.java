package com.SpotLuxe.service.impl;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.SpotLuxe.dto.Result;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.SpotLuxe.entity.ShopType;
import com.SpotLuxe.mapper.ShopTypeMapper;
import com.SpotLuxe.service.IShopTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.SpotLuxe.utils.RedisConstants.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryTypeList() {
        String redisPassword = System.getenv("REDIS_PASSWORD");
        log.info("Redis password from environment variable: {}", redisPassword);
        //1.从redis查询商铺缓存
        String shopTypeJson = stringRedisTemplate.opsForValue().get(CACHE_SHOP_TYPE_KEY);
        List<ShopType>typeList=null;
        //2.判断是否存在
        if(StrUtil.isNotBlank(shopTypeJson)) {
            //3.存在，返回
            typeList= JSONUtil.toList(shopTypeJson, ShopType.class);
            return Result.ok(typeList);
        }
        //4.不存在，查询数据库
        typeList = query().orderByAsc("sort").list();
        //5.将查询到的结果写入redis
        stringRedisTemplate.opsForValue().set(
                CACHE_SHOP_TYPE_KEY,JSONUtil.toJsonStr(typeList),
                CACHE_SHOP_TYPE_TTL, TimeUnit.MINUTES);
        return Result.ok(typeList);
    }
}
