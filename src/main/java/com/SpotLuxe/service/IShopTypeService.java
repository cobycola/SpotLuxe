package com.SpotLuxe.service;

import com.SpotLuxe.dto.Result;
import com.baomidou.mybatisplus.extension.service.IService;
import com.SpotLuxe.entity.ShopType;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IShopTypeService extends IService<ShopType> {
    public Result queryTypeList();
}
