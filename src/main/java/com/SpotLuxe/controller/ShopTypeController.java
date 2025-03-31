package com.SpotLuxe.controller;


import com.SpotLuxe.dto.Result;
import com.SpotLuxe.entity.ShopType;
import com.SpotLuxe.service.IShopTypeService;
import com.SpotLuxe.service.impl.ShopServiceImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 */
@RestController
@RequestMapping("/shop-type")
public class ShopTypeController {
    @Resource
    private IShopTypeService typeService;

    @GetMapping("list")
    public Result queryTypeList() {
        return typeService.queryTypeList();
    }
}
