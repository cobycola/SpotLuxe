package com.SpotLuxe.service;

import com.SpotLuxe.dto.Result;
import com.SpotLuxe.entity.Follow;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.management.Query;

public interface IFollowService extends IService<Follow> {
    Result follow(Long followUserId, Boolean isFollow);

    Result isFollow(Long followUserId);

    Result common(Long id);
}
