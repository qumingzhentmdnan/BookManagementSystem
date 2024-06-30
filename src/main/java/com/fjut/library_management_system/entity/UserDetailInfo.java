package com.fjut.library_management_system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
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
@TableName("user_detail_info")
public class UserDetailInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 身份凭证，学生为学号，教工为教工号
     */
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;



    /**
     * 性别
     */
    @TableField("sex")
    private Boolean sex;

    /**
     * 允许最长借阅时间（天）
     */
    @TableField("borrowing_duration")
    private Integer borrowingDuration;
    /**
     * 证件有效起始时间
     */
    @TableField("certificate_start_date")
    private LocalDate certificateStartDate;

    /**
     * 证件有效终止时间
     */
    @TableField("certificate_end_date")
    private LocalDate certificateEndDate;

    /**
     * 最多可借阅书籍数量
     */
    @TableField("maximum_borrowing_count")
    private Integer maximumBorrowingCount;

    /**
     * 已经借阅书籍数量
     */
    @TableField("already_borrowing_count")
    private Integer alreadyBorrowingCount;

    /**
     * 累计借阅书籍数量
     */
    @TableField("total_borrowing_count")
    private Integer totalBorrowingCount;

    /**
     * 违规次数
     */
    @TableField("number_of_violations")
    private Integer numberOfViolations;

    /**
     * 欠款
     */
    @TableField("owed")
    private BigDecimal owed;

    /**
     * 头像
     */
    @TableField("avatar")
    private String avatar;
}
