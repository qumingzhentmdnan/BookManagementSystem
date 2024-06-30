package com.fjut.library_management_system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fjut.library_management_system.entity.Permission;
import com.fjut.library_management_system.entity.Role;
import com.fjut.library_management_system.vo.UserPermissionNodeVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


import java.util.List;


@Mapper
public interface UserPermissionMapper extends BaseMapper<Permission> {
    List<UserPermissionNodeVo> getPermissionTree(Long userId);

    List<Role> getUserRoles(Long userId);

    List<UserPermissionNodeVo> getRolePermissions(Integer roleId);

    int insertRolePermissions(@Param("list") List<Integer> list, @Param("roleId") Integer roleId);

    int removeRolePermissions(@Param("list")List<Integer> list,@Param("roleId")Integer roleId);

    List<String> getPermissionList(Long userId);
}
