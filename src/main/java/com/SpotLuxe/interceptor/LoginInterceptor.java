package com.SpotLuxe.interceptor;

import com.SpotLuxe.security.context.UserHolder;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(UserHolder.getUser()==null){
            response.setStatus(401);
            return false;
        }
        return true;
    }
}
