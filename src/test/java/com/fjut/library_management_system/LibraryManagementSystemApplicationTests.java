package com.fjut.library_management_system;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fjut.library_management_system.entity.User;
import com.fjut.library_management_system.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LibraryManagementSystemApplicationTests {
//    @Resource
//    private RedisTemplate<String,Object> stringRedisTemplate;
//    @Autowired
//    private UserMapper userMapper;

    @Test
    public void test01(){
//        System.out.println(stringRedisTemplate.hasKey("login:3000000000"));
//        System.out.println(stringRedisTemplate.delete("login:3000000000"));

//        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
//        List<User> users = userMapper.selectList(new QueryWrapper<>());
//        for (User user : users) {
//            Long userId = user.getUserId();
//            String encode = bCryptPasswordEncoder.encode(userId.toString());
//            user.setPassword(encode);
//            userMapper.updateById(user);
//        }
    }
}
