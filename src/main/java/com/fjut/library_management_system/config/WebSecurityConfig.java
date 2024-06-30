package com.fjut.library_management_system.config;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.fjut.library_management_system.service.UserService;
import com.fjut.library_management_system.util.JwtUtil;
import com.fjut.library_management_system.util.Result;
import com.fjut.library_management_system.util.VirtualThreadUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.springframework.security.config.Customizer.withDefaults;


//springsecurity安全配置类
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    @Autowired
    private UserService userService;

    //注入token验证的过滤器
    @Autowired
    private TokenAuthenticationFilterConfig tokenAuthenticationFilterConfig;

    @Resource
    private RedisTemplate<String,Object> stringRedisTemplate;

    //时间戳验证过滤器
    @Autowired
    private TimestampAuthenticationFilterConfig timestampAuthenticationFilterConfig;

    //密码加密为BCryptPasswordEncoder
    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    //从配置文件中读取允许跨域的域名
    @Value("${websecurity.allow.origin}")
    private String origin;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        //anyRequest()：对所有请求开启授权保护
        //authenticated()：已认证请求会自动被授权
        http
                //authorizeRequests()：开启授权保护
                .authorizeRequests(authorize -> authorize
                        //放行请求
                        .requestMatchers("/login","/user/isLogin","websocket/hasMessage").permitAll())
                .formLogin(withDefaults())//表单授权方式
                .httpBasic(withDefaults());//基本授权方式

        http
                //跨域配置
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration configuration = new CorsConfiguration();
                    configuration.setAllowedOrigins(List.of(origin)); //允许跨域的源
                    //configuration.setAllowedOriginPatterns(List.of("*"));
                    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")); // 允许的方法
                    configuration.setAllowedHeaders(List.of("*")); // 允许的头
                    configuration.setExposedHeaders(Arrays.asList("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials")); // 允许暴露的头
                    configuration.setAllowCredentials(true); // 允许携带凭证
                    return configuration;
                }));
        // ...

        //前后端分离，禁用csrf（跨站请求伪造）
        http.csrf(AbstractHttpConfigurer::disable);

        //登录配置
        http.formLogin(
                formLogin -> formLogin
                        .loginPage("/login").permitAll() //登录页面无需授权即可访问
                        //登录成功处理
                        .successHandler(
                                (request, response, authentication) -> {
                                    //查询用户是否绑定手机
                                    CompletableFuture<Boolean> res = VirtualThreadUtil
                                            .executorAsync(()->userService.queryUserHadPhone(Long.valueOf(authentication.getName())));

                                    String name = authentication.getName();

                                    //向Redis中插入数据
                                    VirtualThreadUtil
                                            .executorAsync(() -> stringRedisTemplate.opsForValue().set("login:" + name, JSONObject.toJSONString(authentication.getAuthorities()), 3600, TimeUnit.SECONDS));

                                    //返回json数据
                                    response.setContentType("application/json;charset=utf-8");
                                    //返回是否绑定手机号，以及验证账号的Token
                                    response.getWriter().write(JSON.toJSONString(Result.ok().message("登录成功").data("hasPhone",res.join()).data("token",JwtUtil.createToken(name))));
                                }
                        )

                        //登录失败处理
                        .failureHandler((request, response, exception) -> {
                            //返回json数据
                            response.setContentType("application/json;charset=utf-8");
                            response.getWriter().write(JSON.toJSONString(Result.error().message("登录失败："+exception.getMessage())));
                        })
        );

        //用户登出成功处理
        http.logout(logout -> {
            logout.logoutSuccessHandler((request, response, authentication) -> {
                //移出Redis中的Token
                String token = request.getHeader("Token");
                VirtualThreadUtil
                        .executorAsync(()-> stringRedisTemplate.delete("login:"+JwtUtil.getUsername(token)));
                //System.out.println("登出:login:"+JwtUtil.getUsername(token));
                //返回json数据
                response.setContentType("application/json;charset=utf-8");
                response.getWriter().write(JSON.toJSONString(Result.ok().message("退出成功")));
            });
        });

        //请求未认证的接口
        http.exceptionHandling(exception  -> {
            exception.authenticationEntryPoint((request, response, authException) -> {
                response.setContentType("application/json;charset=utf-8");
                response.getWriter().write(JSON.toJSONString(Result.error().message("未认证")));
            });
        });

        //会话管理,使用无状态的会话token
        http
                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        //添加自定义过滤器
        http
                 //添加时间戳验证过滤器
                .addFilterBefore(timestampAuthenticationFilterConfig, UsernamePasswordAuthenticationFilter.class)
                //添加Token验证过滤器
                .addFilterBefore(tokenAuthenticationFilterConfig, UsernamePasswordAuthenticationFilter.class);

        //返回http配置对象
        return http.build();
    }
}


