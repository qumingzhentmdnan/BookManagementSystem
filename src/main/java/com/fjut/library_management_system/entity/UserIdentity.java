package com.fjut.library_management_system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@TableName("user_identity")
@EqualsAndHashCode
public class UserIdentity implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("identity")
    private String identity;

    @TableField("allow_borrowing_count")
    private int allowBorrowingCount;

    @TableField("allow_borrowing_duration")
    private int allowBorrowingDuration;
}