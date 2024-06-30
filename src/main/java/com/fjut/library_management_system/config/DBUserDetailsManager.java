package com.fjut.library_management_system.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fjut.library_management_system.entity.User;
import com.fjut.library_management_system.mapper.UserMapper;
import com.fjut.library_management_system.mapper.UserPermissionMapper;
import com.fjut.library_management_system.util.VirtualThreadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

//SpringSecurity用户管理
@Component
public class DBUserDetailsManager implements UserDetailsManager, UserDetailsPasswordService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserPermissionMapper userPermissionMapper;

    @Override
    public void createUser(UserDetails user) {

    }

    @Override
    public void updateUser(UserDetails user) {

    }

    @Override
    public void deleteUser(String username) {

    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {

    }

    @Override
    public boolean userExists(String username) {
        return false;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //根据用户名查询用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<User>()
                .eq("user_id", username)
                .eq("is_deleted", 0);

        User user = VirtualThreadUtil
                .executor(() -> userMapper.selectOne(queryWrapper));

        //用户不存在
        if (user == null) {
            throw new UsernameNotFoundException(username);
        } else {
            //用户存在，获取用户权限，封装到GrantedAuthority，返回UserDetails
            Collection<GrantedAuthority> authorities = new ArrayList<>();

            //获取权限列表
            List<String> permissionList = VirtualThreadUtil
                    .executor(() -> userPermissionMapper.getPermissionList(user.getUserId()));

            //封装权限
            for (String permission : permissionList) {
                authorities.add((GrantedAuthority) () -> permission);
            }

            //返回UserDetails
            return new org.springframework.security.core.userdetails.User(
                    user.getUserId().toString(),
                    user.getPassword(),
                    true,
                    true, //用户账号是否过期
                    !user.getExpire(), //用户凭证是否过期
                    !user.getBan(), //用户是否未被锁定
                    authorities); //权限列表
        }
    }

    @Override
    public UserDetails updatePassword(UserDetails user, String newPassword) {
        return null;
    }
}