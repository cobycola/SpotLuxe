package com.SpotLuxe.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.SpotLuxe.dto.Result;
import com.SpotLuxe.dto.UserDTO;
import com.SpotLuxe.entity.Follow;
import com.SpotLuxe.mapper.FollowMapper;
import com.SpotLuxe.security.context.UserHolder;
import com.SpotLuxe.service.IFollowService;
import com.SpotLuxe.service.IUserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.SpotLuxe.common.constant.RedisConstants.FOLLOW_USER_KEY;

@Slf4j
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private IUserService userService;
    @Override
    public Result follow(Long followUserId, Boolean isFollow) {
        //获取当前登录用户ID
        Long userId = UserHolder.getUser().getId();
        String key=FOLLOW_USER_KEY+userId;
        //根据用户ID和关注用户ID判断是否关注
        if (isFollow) {
            //关注
            Follow follow = new Follow();
            follow.setUserId(userId);
            follow.setFollowUserId(followUserId);
            save(follow);
            stringRedisTemplate.opsForSet().add(key,followUserId.toString());
        }else{
            //取消关注
            remove(new QueryWrapper<Follow>()
                    .eq("user_id",userId)
                    .eq("follow_user_id", followUserId));
            stringRedisTemplate.opsForSet().remove(key,followUserId.toString());
        }
        return Result.ok();
    }

    @Override
    public Result isFollow(Long followUserId) {
        //查询是否关注 select * from tb_follow where user_id = ? and follow_user_id = ?
        Long userId = UserHolder.getUser().getId();
        Integer count = query().eq("follow_user_id", followUserId).eq("user_id", userId).count();
        return Result.ok(count>0);
    }

    @Override
    public Result common(Long id) {
        Long userId = UserHolder.getUser().getId();
        //查询共同关注的用户 select * from tb_follow where user_id =? and follow_user_id =?
        String key1=FOLLOW_USER_KEY+userId;
        String key2=FOLLOW_USER_KEY+id;
        Set<String> intersect = stringRedisTemplate.opsForSet().intersect(key1, key2);
        List<Long> ids=intersect.stream().map(Long::valueOf).collect(Collectors.toList());
        List<UserDTO> userDTOs = userService.listByIds(ids)
                .stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .collect(Collectors.toList());
        return Result.ok(userDTOs);
    }
}
