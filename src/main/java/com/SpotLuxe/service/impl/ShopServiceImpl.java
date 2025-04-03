package com.SpotLuxe.service.impl;

import com.SpotLuxe.dto.Result;
import com.SpotLuxe.common.utils.CacheClient;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.SpotLuxe.entity.Shop;
import com.SpotLuxe.mapper.ShopMapper;
import com.SpotLuxe.service.IShopService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.util.concurrent.TimeUnit;

import static com.SpotLuxe.common.constant.RedisConstants.CACHE_SHOP_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private CacheClient cacheClient;

    @Override
    public Result queryById(Long id){
        //查询 缓存穿透
        /*Shop shop=cacheClient.queryWithPassThrough(
                CACHE_SHOP_KEY,id,Shop.class,
                this::getById,
                CACHE_SHOP_TTL,TimeUnit.MINUTES);*/
        //查询 缓存击穿
        Shop shop=cacheClient.queryWithLogicalExpire(
                CACHE_SHOP_KEY,id,Shop.class,
                this::getById,
                20L,TimeUnit.SECONDS);

        if(shop==null){
            return Result.fail("店铺不存在");
        }
        return Result.ok(shop);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result update(Shop shop) {
        try{
            //1.获取店铺id
            Long id = shop.getId();
            //2.判断店铺id是否为空
            if (id == null) {
                return Result.fail("店铺id不能为空");
            }
            //2.根据店铺id对对应的数据库进行更新
            updateById(shop);
            //3.删除redis中的店铺缓存
            stringRedisTemplate.delete(CACHE_SHOP_KEY + id);
            return Result.ok();
        }catch(Exception e){
            return Result.fail("更新店铺信息失败，店铺id:"+shop.getId());
        }

    }
}
