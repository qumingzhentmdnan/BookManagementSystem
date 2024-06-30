package com.fjut.library_management_system.config;

import com.alibaba.fastjson2.JSON;
import com.fjut.library_management_system.util.JwtUtil;
import com.fjut.library_management_system.util.Result;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

//token验证过滤器
@Component
public class TokenAuthenticationFilterConfig extends OncePerRequestFilter {
    //此处注入的是StringRedisTemplate，RedisTemplate子类
    @Resource
    private RedisTemplate<String,Object> stringRedisTemplate;

    //每次请求都会执行这个方法
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String token = request.getHeader("Token");

        //token为空
        if (token == null || token.isEmpty()) {
            // 如果是需要放行的URL，直接放行，不进行后续的过滤器处理(websocket心跳链接不携带token，所以直接放心)
            if ("/login".equals(requestURI)||"/websocket/hasMessage".equals(requestURI)) {
                // 如果是需要放行的URL，直接放行，不进行后续的过滤器处理
                filterChain.doFilter(request, response);
            }else{
                //需要验证才能访问的URL，返回登录状态已过期，请重新登录
                response.setContentType("application/json;charset=utf-8");
                response.getWriter().write(JSON.toJSONString(Result.error().message("登录状态已过期，请重新登录").data("isLogin", false).data("reason", "token为空")));
            }
            return;
        }

        //token无效
        boolean tokenValid = JwtUtil.isTokenValid(token);
        if (!tokenValid) {
            // 如果是需要放行的URL，直接放行，不进行后续的过滤器处理
            if ("/login".equals(requestURI)) {
                filterChain.doFilter(request, response);
            }else{
                //需要验证才能访问的URL，返回登录状态已过期，请重新登录
                response.setContentType("application/json;charset=utf-8");
                response.getWriter().write(JSON.toJSONString(Result.error().message("登录状态已过期，请重新登录").data("isLogin", false).data("reason", "token无效")));
            }
            return;
        }

        // 获取userid 从redis中获取用户信息,key为login:userId，value为权限列表
        String userId = JwtUtil.getUsername(token);
        String redisKey = "login:" + userId;
        Object o = stringRedisTemplate.opsForValue().get(redisKey);

        //redis中没有用户信息,返回登录状态已过期，请重新登录
        if (Objects.isNull(o)) {
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write(JSON.toJSONString(Result.error().message("登录状态已过期，请重新登录").data("isLogin", false)));
            return;
        }

        //将权限信息存入到SecurityContextHolder
        List<GrantedAuthority> grantedAuthorities = JSON.parseArray(o.toString(), GrantedAuthority.class);
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userId, null, grantedAuthorities);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        //放行
        filterChain.doFilter(request, response);
    }
}