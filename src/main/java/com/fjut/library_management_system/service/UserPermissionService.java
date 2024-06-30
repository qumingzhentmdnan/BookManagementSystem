package com.fjut.library_management_system.service;

import com.fjut.library_management_system.entity.Role;
import com.fjut.library_management_system.util.Result;
import com.fjut.library_management_system.vo.PermissionVo;
import com.fjut.library_management_system.vo.RoleVo;
import com.fjut.library_management_system.vo.UserPermissionNodeVo;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 叶良辰
 * @since 2024年03月31日
 */
public interface UserPermissionService {

    List<UserPermissionNodeVo> getUserPermissionsTree(Long userId);

     List<UserPermissionNodeVo> getAllPermissions();

     List<Role> getAllRoles();
    List<Role> getUserRoles(Long userId);

    boolean assignRole(Long userId, Integer roleId);

    boolean removeRole(Long userId, Integer roleId);

    List<UserPermissionNodeVo> getRolePermissionsTree(Integer roleId);

    boolean updatePermissionInfo(List<Integer> addPermissionList, List<Integer> removePermissionList, Integer roleId);

    List<UserPermissionNodeVo> getAllPermissionsTree();


    Result addRole(RoleVo roleVo);

    boolean deleteRole(Integer roleId);
}
