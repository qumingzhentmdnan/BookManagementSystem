package com.fjut.library_management_system.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.validator.constraints.Range;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@ToString
@EqualsAndHashCode(callSuper = false)
public class AddUserVo implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 身份凭证，学生为学号，教工为教工号
     */
    @NotNull
    @Range(min=1000000000, max=9999999999L, message = "请输入正确的10位身份凭证")
    @ExcelProperty("身份凭证")
    private Long userId;

    /**
     * 姓名
     */
    @NotNull
    @Size(max = 20, message = "用户名长度不能超过20")
    @ExcelProperty("姓名")
    private String userName;


    /**
     * 归属部门或班级
     */
    @NotNull
    @Size(max = 30, message = "归属部门或班级长度不能超过30")
    @ExcelProperty("归属部门或班级")
    private String department;

    /**
     * 身份
     */
    @NotNull
    @Size(max = 20, message = "身份长度不能超过20")
    @ExcelProperty("身份")
    private String identity;

    /**
     * 性别
     */
    @NotNull
    @ExcelProperty("性别")
    @Pattern(regexp = "男|女", message = "性别只能为男或者女")
    private String  sex;

    @NotNull
    @ExcelProperty("证件终止时间")
    private LocalDate certificateEndDate;
}