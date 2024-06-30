package com.fjut.library_management_system.vo;

import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class RoleVo implements Serializable {
    List<Integer> permissionList;
    @Pattern(regexp = "^.{0,20}$", message = "角色名的长度必须在0到20个字符之间")
    String roleName;
    @Pattern(regexp = "^.{0,50}$", message = "角色描述的长度必须在0到100个字符之间")
    String roleDescription;
}