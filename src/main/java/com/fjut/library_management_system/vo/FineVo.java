package com.fjut.library_management_system.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fjut.library_management_system.util.excel.FineVoConverter;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = false)
public class FineVo implements Serializable {
    @ExcelProperty("书籍名称")
    @ColumnWidth(40)
    /**
     * 书籍名称
     */
    @TableField("book_name")
    private String bookName;

    @ExcelProperty("违规用户凭证")
    @ColumnWidth(20)
    /**
     * 身份凭证，学生为学号，教工为教工号
     */
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    /**
     * 姓名
     */
    @ExcelProperty("违规用户姓名")
    @ColumnWidth(20)
    @TableField("user_name")
    private String userName;


    @ExcelProperty("罚款金额")
    @ColumnWidth(20)
    /**
     * 罚款金额
     */
    @TableField("fine_price")
    private BigDecimal finePrice;

    @ExcelProperty(value = "是否支付",converter = FineVoConverter.class)
    @ColumnWidth(20)
    /**
     * 是否支付
     */
    @TableField("payed")
    private Boolean payed;


    @ExcelProperty("缴费流水号")
    @ColumnWidth(20)
    /**
     * 缴费流水号
     */
    @TableField("fine_id")
    private String fineId;


    @ExcelProperty("支付日期")
    @ColumnWidth(20)
    /**
     * 支付日期
     */
    @TableField("paying_date")
    private LocalDate payingDate;
}