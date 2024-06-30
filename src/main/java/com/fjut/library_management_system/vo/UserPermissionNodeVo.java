package com.fjut.library_management_system.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class UserPermissionNodeVo implements Serializable {
    /**
     * 权限id
     */
    @TableId(value = "permission_id", type = IdType.AUTO)
    private Integer permissionId;

    /**
     * 权限父id
     */
    @TableField(value = "parent_id")
    private Integer parentId;

    /**
     * 权限描述
     */
    @TableField("description")
    private String description;

    /**
     * 是否有当前权限
     */
    private boolean hasPermission;

    private List<UserPermissionNodeVo> children=new ArrayList<>();
}