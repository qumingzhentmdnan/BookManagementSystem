package com.fjut.library_management_system.vo;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class PermissionVo implements Serializable {
    List<Integer> addPermissionList;
    List<Integer> removePermissionList;

    @NotNull(message = "角色id不能为空")
    @Digits(integer = 9, fraction = 0, message = "角色id必须为数字")
    Integer roleId;
}