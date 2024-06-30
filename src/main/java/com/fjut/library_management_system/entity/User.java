package com.fjut.library_management_system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Objects;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
@TableName("user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 身份凭证，学生为学号，教工为教工号
     */
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    /**
     * 密码
     */
    @TableField("password")
    private String password;

    /**
     * 绑定手机号
     */
    @TableField("phone")
    private Long phone;

    /**
     * 姓名
     */
    @TableField("user_name")
    private String userName;

    @TableField("has_message")
    private boolean hasMessage;

    /**
     * 归属部门或班级
     */
    @TableField("department")
    private String department;

    /**
     * 身份
     */
    @TableField("identity")
    private String identity;

    /**
     * 该账号是否封禁
     */
    @TableField("ban")
    private Boolean ban;

    /**
     * 该账号凭证是否过期
     */
    @TableField("expire")
    private Boolean expire;
}
