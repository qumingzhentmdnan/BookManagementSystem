package com.fjut.library_management_system.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fjut.library_management_system.util.excel.BorrowingConverter;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = false)
public class BorrowingVo implements Serializable {
    /**
     * 身份凭证，学生为学号，教工为教工号
     */
    @TableId(value = "user_id", type = IdType.AUTO)
    @ExcelProperty("用户凭证")
    @ColumnWidth(20)
    private Long userId;

    @ExcelProperty("借阅用户名称")
    @ColumnWidth(20)
    @TableField("user_name")
    private String userName;

    @ExcelProperty("书籍名称")
    @ColumnWidth(40)
    @TableField("book_name")
    private String bookName;

    @ExcelProperty("isbn")
    @ColumnWidth(40)
    @TableField("isbn")
    private String isbn;

    @ExcelProperty("索书号")
    @ColumnWidth(40)
    @TableField("call_number")
    private String callNumber;

    /**
     * 借书时间
     */
    @ExcelProperty("借阅时间")
    @ColumnWidth(20)
    @TableField("borrowing_date")
    private LocalDate borrowingDate;

    /**
     * 还书时间
     */
    @ExcelProperty("归还时间")
    @ColumnWidth(20)
    @TableField("return_date")
    private LocalDate returnDate;

    /**
     * 是否归还
     */
    @ExcelProperty(value = "归还状态",converter = BorrowingConverter.ReturnedStatus.class)
    @ColumnWidth(20)
    @TableField("returned")
    private Boolean returned;

    /**
     * 是否超时
     */
    @ExcelProperty(value = "超时状态",converter = BorrowingConverter.OvertimeStatus.class)
    @ColumnWidth(20)
    @TableField("overtime")
    private Boolean overtime;
}