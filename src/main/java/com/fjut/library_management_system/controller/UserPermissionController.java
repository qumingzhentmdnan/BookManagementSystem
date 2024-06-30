package com.fjut.library_management_system.controller;

import com.fjut.library_management_system.service.UserPermissionService;
import com.fjut.library_management_system.util.RedisUtil;
import com.fjut.library_management_system.util.Result;
import com.fjut.library_management_system.util.VirtualThreadUtil;
import com.fjut.library_management_system.vo.PermissionVo;
import com.fjut.library_management_system.vo.RoleVo;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Digits;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/userPermission")
public class UserPermissionController {
    private final UserPermissionService userPermissionService;

    @Resource
    private RedisTemplate<String, Object> stringRedisTemplate;

    @Autowired
    public UserPermissionController(UserPermissionService userPermissionService) {
        this.userPermissionService = userPermissionService;
    }

    @Value("${system.admin.accout}")
    private Long ADMIN_ID;

    /**
     * 方法作用：根据用户Id查询用户权限
     */
    @Cacheable(value = "userPermissions", key = "#userId")
    @GetMapping("/getUserPermission")
    public Result getUserPermission(@Range(min=1000000000, max=9999999999L, message = "请输入正确的10位身份凭证")
                                    Long userId) {
        return Result.ok().data("permission", userPermissionService.getUserPermissionsTree(userId));
    }

    /**
     * 得到所有的角色
     */
    @Cacheable(value = "roles", key = "'roles'")
    @GetMapping("/getAllRoles")
    public Result getAllRoles() {
        return Result.ok().data("roles", userPermissionService.getAllRoles());
    }

    /**
     * 得到用户的所有的角色
     */
    @Cacheable(value = "userRoles", key = "#userId")
    @PreAuthorize("hasAuthority('queryUserInfo')")
    @GetMapping("/getUserRoles")
    public Result getUserRoles(@Range(min=1000000000, max=9999999999L, message = "请输入正确的10位身份凭证")
                               Long userId) {
        return Result.ok().data("roles", userPermissionService.getUserRoles(userId));
    }

    /**
     * 给用户分配一个角色
     */
    @Caching(evict = {
            @CacheEvict(value = "userPermissions", key = "#userId"),
            @CacheEvict(value = "userRoles", key = "#userId"),
            @CacheEvict(value = "globalUserInfo", key = "#userId")
    })
    @PreAuthorize("hasAuthority('assignRoles')")
    @PostMapping("/assignRole")
    public Result addUserRole(@Range(min=1000000000, max=9999999999L, message = "请输入正确的10位身份凭证") Long userId,
                              @Digits(integer = 9, fraction = 0, message = "角色id必须为数字") Integer roleId) {
        if (roleId == 1) {
            return Result.error().message("无权限修改超级管理员的权限");
        }
        VirtualThreadUtil.executorAsync(()-> stringRedisTemplate.delete("login:" + userId));
        boolean res = userPermissionService.assignRole(userId, roleId);
        return res ? Result.ok().message("分配成功") : Result.error().message("分配失败");
    }

    /**
     * 移除一个角色
     */
    @Caching(evict = {
            @CacheEvict(value = "userPermissions", key = "#userId"),
            @CacheEvict(value = "userRoles", key = "#userId"),
            @CacheEvict(value = "globalUserInfo", key = "#userId")
    })
    @PreAuthorize("hasAuthority('removeRoles')")
    @DeleteMapping("/removeRole")
    public Result removeRole(@Range(min=1000000000, max=9999999999L, message = "请输入正确的10位身份凭证") Long userId,
                             @Digits(integer = 9, fraction = 0, message = "角色id必须为数字") Integer roleId) {
        if (roleId == 1) {
            return Result.error().message("无权限修改超级管理员的权限");
        }
        //将被移除的角色的用户的session全部过期，使其重新获得session以更新权限
        VirtualThreadUtil.executorAsync(()-> stringRedisTemplate.delete("login:" + userId));
        boolean res = userPermissionService.removeRole(userId, roleId);
        return res ? Result.ok().message("分配成功") : Result.error().message("分配失败");
    }

    /**
     * 得到角色对应的权限
     */
    @Cacheable(value = "rolePermissions", key = "#roleId")
    @PreAuthorize("hasAuthority('queryRole')")
    @GetMapping("/getRolePermissions")
    public Result getRolePermissions(@Digits(integer = 9, fraction = 0, message = "角色id必须为数字") Integer roleId) {
        return Result.ok().data("permissions", userPermissionService.getRolePermissionsTree(roleId));
    }

    /**
     * 方法作用：修改角色对应的权限
     */
    @Caching(evict = {
            @CacheEvict(value = "rolePermissions", key = "#permissionVo.getRoleId()"),
            @CacheEvict(value = "userPermissions", allEntries = true),
            @CacheEvict(value = "globalUserInfo", allEntries = true)
    })
    @PreAuthorize("hasAuthority('updateRole')")
    @PutMapping("/updatePermissionInfo")
    public Result updatePermissionInfo(@RequestBody PermissionVo permissionVo) {
        if (permissionVo.getRoleId() == 1) {
            return Result.error().message("无权限修改超级管理员");
        }
        //删除缓存
        RedisUtil redisUtil = new RedisUtil();
        VirtualThreadUtil.executorAsync(()->redisUtil.removeCacheByPrefix("login:"));
        VirtualThreadUtil.executorAsync(()-> redisUtil.removeCacheByPrefix("globalUserInfo"));

        boolean res = userPermissionService.updatePermissionInfo(permissionVo.getAddPermissionList(),
                permissionVo.getRemovePermissionList(), permissionVo.getRoleId());
        return res ? Result.ok() : Result.error();
    }

    /**
     * 方法作用：获取所有权限
     */
    @Cacheable(value = "permissions", key = "'permissions'")
    @GetMapping("/getAllPermissions")
    public Result getAllPermissions() {
        return Result.ok().data("permissions", userPermissionService.getAllPermissionsTree());
    }

    /**
     * 方法作用：添加一个角色，并分配角色的权限
     */
    @CacheEvict(value = "roles", key = "'roles'")
    @PreAuthorize("hasAuthority('addRole')")
    @PostMapping("/addRole")
    public Result addRole(@RequestBody RoleVo roleVo) {
        return userPermissionService.addRole(roleVo);
    }

    /**
     * 方法作用：删除一个角色，并删除角色的权限
     */
    @Caching(evict = {
            @CacheEvict(value = "roles", key = "'roles'"),
            @CacheEvict(value = "rolePermissions", key = "#roleId"),
            @CacheEvict(value = "userRoles", allEntries = true),
            @CacheEvict(value = "userPermissions", allEntries = true),
            @CacheEvict(value = "globalUserInfo", allEntries = true)
    })
    @PreAuthorize("hasAuthority('deleteRole')")
    @DeleteMapping("/deleteRole")
    public Result deleteRole(@Digits(integer = 9, fraction = 0, message = "角色id必须为数字") Integer roleId) {
        if (roleId == 1) {
            return Result.error().message("无权限修改超级管理员的权限");
        }
        RedisUtil redisUtil = new RedisUtil();
        redisUtil.removeCacheByPrefix("login:");
        redisUtil.removeCacheByPrefix("globalUserInfo");
        return Result.ok().data("permissions", userPermissionService.deleteRole(roleId));
    }
}