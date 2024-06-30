package com.fjut.library_management_system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author 叶良辰
 * @since 2024年03月31日
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("role")
public class Role implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 权限角色的id
     */
    @TableId(value = "role_id", type = IdType.AUTO)
    private Integer roleId;

    /**
     * 权限角色名称
     */
    @TableField("role_name")
    private String roleName;

    /**
     * 权限角色描述
     */
    @TableField("description")
    private String description;

    /**
     * 角色创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
}
