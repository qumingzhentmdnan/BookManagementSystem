package com.fjut.library_management_system.mapper;

import com.fjut.library_management_system.entity.Role;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author 叶良辰
 * @since 2024年03月31日
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<Role> {
    int assignRole(Long userId, Integer roleId);

    int removeRole(Long userId, Integer roleId);
}
