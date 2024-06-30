package com.fjut.library_management_system.vo;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Range;

import java.io.Serializable;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class QueryUserVo implements Serializable {
    @Range(min=1000000000, max=9999999999L, message = "请输入正确的10位身份凭证")
    Long userId;

    @Pattern(regexp = "^.{0,20}$", message = "用户名的长度必须在0到20个字符之间")
    String userName;

    @Digits(integer = 18, fraction = 0, message = "用户号码必须为数字")
    Long userPhone;

    @Pattern(regexp = "^.{0,20}$", message = "用户部门的长度必须在0到20个字符之间")
    String userDepartment;

    @Pattern(regexp = "^.{0,20}$", message = "用户身份的长度必须在0到20个字符之间")
    String userIdentity;

    @Min(value = 1, message = "当前页不能为负数")
    @NotNull(message = "当前页数不能为空")
    private Integer page;


    @Min(value = 1, message = "分页行数必须大于等于1")
    @Max(value = 100, message = "分页行数不超过100")
    @NotNull(message = "分页行数不能为空")
    private Integer limit;
}