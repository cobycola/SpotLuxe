package com.SpotLuxe.service;

import com.SpotLuxe.dto.Result;
import com.baomidou.mybatisplus.extension.service.IService;
import com.SpotLuxe.entity.Shop;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IShopService extends IService<Shop> {


    Result queryById(Long id);

    Result update(Shop shop);
}
