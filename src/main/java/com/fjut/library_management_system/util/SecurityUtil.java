package com.fjut.library_management_system.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

//获取当前登录用户的工具类
public class SecurityUtil {
    public static Long getCurrentUsername() {
        SecurityContext context = SecurityContextHolder.getContext();//存储认证对象的上下文
        Authentication authentication = context.getAuthentication();//认证对象
        String username = authentication.getName();//用户名
        return Long.valueOf(username);
    }
}