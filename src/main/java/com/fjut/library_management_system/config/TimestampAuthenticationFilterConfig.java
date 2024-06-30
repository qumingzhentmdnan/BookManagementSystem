package com.fjut.library_management_system.config;

import com.alibaba.fastjson2.JSONObject;
import com.fjut.library_management_system.util.Result;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
// 自定义过滤器
public  class TimestampAuthenticationFilterConfig extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        // 将ServletRequest转换为HttpServletRequest
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        // 检查是否是WebSocket心跳请求
        String requestURI = httpRequest.getRequestURI();
        if (requestURI != null && requestURI.contains("/websocket")) {
            // 如果是WebSocket心跳请求，直接放行
            chain.doFilter(request, response);
            return;
        }

        // 获取请求头中的timestamp
        String timestamp = httpRequest.getHeader("Timestamp");
        if(timestamp== null){
            // 将ServletResponse转换为HttpServletResponse
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            // 设置HTTP响应的内容类型和字符编码
            httpResponse.setContentType("application/json;charset=utf-8");
            // 设置HTTP响应的状态码和消息
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().write(JSONObject.toJSONString(Result.error().message("非法请求")));
            return;
        }

        // 提取出校验位
        int checksumInTimestamp = Character.getNumericValue(timestamp.charAt(9));
        // 用第一位的值初始化校验位
        int checksum = Character.getNumericValue(timestamp.charAt(0));

        // 遍历每一位（除了第四位）
        for (int i = 1; i < timestamp.length(); i++) {
            if(i==8){
                checksum*=Character.getNumericValue(timestamp.charAt(i));
                continue;
            }
            if (i != 9) {
                // 更新校验位
                checksum += Character.getNumericValue(timestamp.charAt(i));
            }
        }
        // 比较计算出的校验位和时间戳的第四位和时间戳的有效期
        if(checksum%10 != checksumInTimestamp||Math.abs(System.currentTimeMillis()-Long.parseLong(timestamp))> 60000){
            // 将ServletResponse转换为HttpServletResponse
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            // 设置HTTP响应的内容类型和字符编码
            httpResponse.setContentType("application/json;charset=utf-8");
            // 设置HTTP响应的状态码和消息
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().write(JSONObject.toJSONString(Result.error().message("非法请求")));
            return;
        }
        // 放行
        chain.doFilter(request, response);
    }
}