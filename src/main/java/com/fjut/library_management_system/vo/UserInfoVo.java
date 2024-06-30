package com.fjut.library_management_system.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fjut.library_management_system.util.excel.UserInfoConverter;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = false)
public class UserInfoVo implements Serializable {
    /**
     * 身份凭证，学生为学号，教工为教工号
     */
    @ExcelProperty("身份凭证")
    @ColumnWidth(20)
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    /**
     * 绑定手机号
     */
    @ExcelProperty("绑定手机号")
    @ColumnWidth(20)
    @TableField("phone")
    private Long phone;

    /**
     * 姓名
     */
    @ExcelProperty("姓名")
    @ColumnWidth(20)
    @TableField("user_name")
    private String userName;

    /**
     * 归属部门或班级
     */
    @ExcelProperty("归属部门或班级")
    @ColumnWidth(20)
    @TableField("department")
    private String department;

    /**
     * 身份
     */
    @ExcelProperty("身份")
    @ColumnWidth(20)
    @TableField("identity")
    private String identity;

    /**
     * 性别
     */
    @ExcelProperty(value = "性别",converter = UserInfoConverter.class)
    @ColumnWidth(20)
    @TableField("sex")
    private Boolean sex;


    /**
     * 证件有效起始时间
     */
    @ExcelProperty("证件起始时间")
    @ColumnWidth(20)
    @TableField("certificate_start_date")
    private LocalDate certificateStartDate;

    /**
     * 证件有效终止时间
     */
    @ExcelProperty("证件终止时间")
    @ColumnWidth(20)
    @TableField("certificate_end_date")
    private LocalDate certificateEndDate;

    /**
     * 最多可借阅书籍数量
     */
    @ExcelProperty("最多可借阅书籍数量")
    @ColumnWidth(20)
    @TableField("maximum_borrowing_count")
    private Integer maximumBorrowingCount;

    /**
     * 已经借阅书籍数量
     */
    @ExcelProperty("已经借阅书籍数量")
    @ColumnWidth(20)
    @TableField("already_borrowing_count")
    private Integer alreadyBorrowingCount;

    /**
     * 累计借阅书籍数量
     */
    @ExcelProperty("累计借阅书籍数量")
    @ColumnWidth(20)
    @TableField("total_borrowing_count")
    private Integer totalBorrowingCount;

    /**
     * 违规次数
     */
    @ExcelProperty("违规次数")
    @ColumnWidth(20)
    @TableField("number_of_violations")
    private Integer numberOfViolations;

    /**
     * 欠款
     */
    @ExcelProperty("欠款")
    @ColumnWidth(20)
    @TableField("owed")
    private BigDecimal owed;

    /**
     * 头像
     */
    @ExcelIgnore
    @TableField("avatar")
    private String avatar;
}