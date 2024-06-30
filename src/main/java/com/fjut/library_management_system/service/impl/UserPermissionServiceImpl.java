package com.fjut.library_management_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fjut.library_management_system.entity.Permission;
import com.fjut.library_management_system.entity.Role;
import com.fjut.library_management_system.entity.RolePermission;
import com.fjut.library_management_system.mapper.UserPermissionMapper;
import com.fjut.library_management_system.mapper.UserRoleMapper;
import com.fjut.library_management_system.service.UserPermissionService;
import com.fjut.library_management_system.util.Result;
import com.fjut.library_management_system.util.VirtualThreadUtil;
import com.fjut.library_management_system.vo.RoleVo;
import com.fjut.library_management_system.vo.UserPermissionNodeVo;
import org.apache.commons.collections4.map.LinkedMap;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 叶良辰
 * @since 2024年03月31日
 */
@Service
@Transactional
public class UserPermissionServiceImpl implements UserPermissionService {

    private final UserPermissionMapper userPermissionMapper;

    private final UserRoleMapper userRoleMapper;

    public UserPermissionServiceImpl(UserPermissionMapper userPermissionMapper, UserRoleMapper userRoleMapper) {
        this.userPermissionMapper = userPermissionMapper;
        this.userRoleMapper = userRoleMapper;
    }

    //得到用户权限森林
    @Override
    public List<UserPermissionNodeVo> getUserPermissionsTree(Long userId) {
        //得到用户权限
        List<UserPermissionNodeVo> userPermissionNodeVoList = VirtualThreadUtil.executor(()->userPermissionMapper.getPermissionTree(userId));

        //转化为map
        LinkedMap<Integer, UserPermissionNodeVo> userPermissionMap = getPermissionMap(userPermissionNodeVoList);

        //得到所有权限
        List<UserPermissionNodeVo> allPermissionNodeVos = getAllPermissions();
        //转化为map
        LinkedMap<Integer, UserPermissionNodeVo> allPermissionMap = getPermissionMap(allPermissionNodeVos);

        //返回权限森林
        return getPermissionTree(allPermissionMap, userPermissionMap);
    }

    //得到某一个角色的权限
    @Override
    public List<UserPermissionNodeVo> getRolePermissionsTree(Integer roleId) {
        //得到所有权限
        List<UserPermissionNodeVo> allPermissionNodeVos = getAllPermissions();
        //转化为map
        LinkedMap<Integer, UserPermissionNodeVo> allPermissionMap = getPermissionMap(allPermissionNodeVos);

        //得到角色权限
        List<UserPermissionNodeVo> rolePermissionsNodeVos = VirtualThreadUtil.executor(()->userPermissionMapper.getRolePermissions(roleId));
        //转化为map
        LinkedMap<Integer, UserPermissionNodeVo> rolePermissionMap = getPermissionMap(rolePermissionsNodeVos);

        return getPermissionTree(allPermissionMap, rolePermissionMap);
    }

    //更新角色权限
    @Override
    public boolean updatePermissionInfo(List<Integer> addPermissionList, List<Integer> removePermissionList, Integer roleId) {

        CompletableFuture<Boolean> res1=CompletableFuture.completedFuture(true);
        CompletableFuture<Boolean> res2=CompletableFuture.completedFuture(true);

        //插入新加的权限
        if(!addPermissionList.isEmpty()){
            res1 = VirtualThreadUtil.executorAsync(() -> userPermissionMapper.insertRolePermissions(addPermissionList, roleId) > 0);
        }
        //删除移出的权限
        if(!removePermissionList.isEmpty()){
            res2=VirtualThreadUtil.executorAsync(()->userPermissionMapper.removeRolePermissions(removePermissionList,roleId)>0);
        }

        return res1.thenCombine(res2,(a,b)->a&b).join();
    }

    //得到所有权限树
    @Override
    public List<UserPermissionNodeVo> getAllPermissionsTree() {
        //得到所有权限
        List<UserPermissionNodeVo> allPermissions = getAllPermissions();
        //转化为map
        LinkedMap<Integer, UserPermissionNodeVo> allPermissionMap = getPermissionMap(allPermissions);
        ArrayList<UserPermissionNodeVo> allPermissionTree = new ArrayList<>();

        //构建权限森林
        for (UserPermissionNodeVo permission : allPermissions) {
            //如果是根节点(parentId=0的节点)，直接加入权限森林，否则加入其父节点的children中
            if(permission.getParentId()==0)
                allPermissionTree.add(permission);
            else
                allPermissionMap.get(permission.getParentId()).getChildren().add(permission);
        }
        //返回权限森林
        return allPermissionTree;
    }

    //添加角色
    @Override
    public Result addRole(RoleVo roleVo) {
        //判断是否已有该角色
        if(VirtualThreadUtil.executor(()->userRoleMapper.selectOne(new QueryWrapper<Role>().eq("is_deleted", 0)
                .eq("role_name",roleVo.getRoleName()))!=null)){
            return Result.error().message("已有的角色无法再次添加");
        }
        //插入角色
        Role role = new Role().setRoleName(roleVo.getRoleName()).setDescription(roleVo.getRoleDescription());

        CompletableFuture<Boolean> res1 = VirtualThreadUtil.executorAsync(() -> userRoleMapper.insert(role) > 0);
        CompletableFuture<Boolean> res2 = CompletableFuture.completedFuture(true);

        //插入角色权限
        if(!roleVo.getPermissionList().isEmpty())
            res2=VirtualThreadUtil.executorAsync(()->userPermissionMapper.insertRolePermissions(roleVo.getPermissionList(), role.getRoleId())>0);

        //返回结果
        return  res1.thenCombine(res2,(a,b)->a&b).join()?Result.ok().message("添加角色成功"):Result.error().message("添加角色失败");
    }

    //删除角色
    @Override
    public boolean deleteRole(Integer roleId) {
        //删除角色
        return VirtualThreadUtil.executor(()->userRoleMapper.update(new UpdateWrapper<Role>().set("is_deleted", 1).eq("role_id", roleId)) > 0);
    }


    //将list转换为map
    private LinkedMap<Integer, UserPermissionNodeVo> getPermissionMap(List<UserPermissionNodeVo> permission){
        LinkedMap<Integer, UserPermissionNodeVo> userPermissionMap = new LinkedMap<>();
        //将所有权限放入map中，以Id作为键值
        for (UserPermissionNodeVo userPermissionNodeVo : permission) {
            userPermissionMap.put(userPermissionNodeVo.getPermissionId(), userPermissionNodeVo);
        }
        return userPermissionMap;
    }

    //构建用户的权限森林，part为用户拥有的权限，all为所有权限
    private List<UserPermissionNodeVo> getPermissionTree(LinkedMap<Integer, UserPermissionNodeVo> all,LinkedMap<Integer, UserPermissionNodeVo> part){
        ArrayList<UserPermissionNodeVo> permissionTree = new ArrayList<>();
        //遍历所有节点
        for (UserPermissionNodeVo node : all.values()) {
            //当前节点是否存在于part中，存在设置hasPermission为true，否则为false
            node.setHasPermission(part.containsKey(node.getPermissionId()));

            //如果是根节点(parentId=0的节点)，直接加入权限森林，否则加入其父节点的children中
            if(node.getParentId()==0)
                permissionTree.add(node);
            else
                all.get(node.getParentId()).getChildren().add(node);
        }
        //返回权限森林
        return permissionTree;
    }

    //得到所有权限,Permission类型
    @Override
    public List<UserPermissionNodeVo> getAllPermissions() {
        List<Permission> permissions = VirtualThreadUtil.executor(()->userPermissionMapper.selectList(new QueryWrapper<Permission>()
                .select("permission_id", "description", "parent_id", "permission_name").eq("is_deleted", 0)));
        ArrayList<UserPermissionNodeVo> userPermissionNodeVos = new ArrayList<>(permissions.size());

        //将其封装到UserPermissionNode中
        for (Permission permission : permissions) {
            UserPermissionNodeVo userPermissionNodeVo = new UserPermissionNodeVo();
            BeanUtils.copyProperties(permission, userPermissionNodeVo);
            userPermissionNodeVos.add(userPermissionNodeVo);
        }
        //返回
        return userPermissionNodeVos;
    }

    //得到所有角色
    @Override
    public List<Role> getAllRoles() {
        return VirtualThreadUtil
                .executor(()->userRoleMapper.selectList(new QueryWrapper<Role>().select( "role_id", "role_name", "description", "create_time").eq("is_deleted", 0)));
    }

    //得到用户所拥有的所有角色
    @Override
    public List<Role> getUserRoles(Long userId) {
        return  VirtualThreadUtil.executor(()->userPermissionMapper.getUserRoles(userId));
    }

    //为用户分配某一个角色
    @Override
    public boolean assignRole(Long userId, Integer roleId) {
        return  VirtualThreadUtil.executor(()->userRoleMapper.assignRole(userId, roleId)>0);
    }

    //移除用户的某一个角色
    @Override
    public boolean removeRole(Long userId, Integer roleId) {
        return VirtualThreadUtil.executor(()->userRoleMapper.removeRole(userId, roleId)>0);
    }
}
