package com.SpotLuxe.interceptor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.SpotLuxe.security.context.UserHolder;
import com.SpotLuxe.dto.UserDTO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.SpotLuxe.common.constant.RedisConstants.LOGIN_USER_KEY;
import static com.SpotLuxe.common.constant.RedisConstants.LOGIN_USER_TTL;

public class RefreshTokenInterceptor implements HandlerInterceptor {
    private StringRedisTemplate stringRedisTemplate;
    public  RefreshTokenInterceptor (StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1.获取请求头中的token
        String token = request.getHeader("authorization");
        if(StrUtil.isBlank(token)){
            return true;
        }
        //2.根据token获得redis里的用户
        String tokenKey=LOGIN_USER_KEY+token;
        Map<Object,Object> userMap=stringRedisTemplate.opsForHash().entries(tokenKey);
        //3.判断用户是否存在
        if(userMap.isEmpty()){
            return true;
        }
        //5.将查询到的hash数据转为UserDTO
        UserDTO user= BeanUtil.fillBeanWithMap(userMap,new UserDTO(),false);
        //6.存在，保存用户信息到ThreadLocal
        UserHolder.saveUser(user);
        stringRedisTemplate.expire(tokenKey,LOGIN_USER_TTL, TimeUnit.SECONDS);
        //7.放行
        return true;
    }
}
