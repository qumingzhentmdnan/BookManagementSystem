package com.fjut.library_management_system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDate;

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
@TableName("book_borrowing_info")
public class BookBorrowingInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 借书id
     */
    @TableId(value = "borrowing_id", type = IdType.AUTO)
    private Long borrowingId;

    /**
     * 图书id
     */
    @TableField("book_id")
    private Integer bookId;

    /**
     * 用户id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 储存id
     */
    @TableField("store_id")
    private Integer storeId;
    /**
     * 借书时间
     */
    @TableField("borrowing_date")
    private LocalDate borrowingDate;

    /**
     * 还书时间
     */
    @TableField("return_date")
    private LocalDate returnDate;

    /**
     * 是否归还
     */
    @TableField("returned")
    private Boolean returned;

    /**
     * 是否超时
     */
    @TableField("overtime")
    private Boolean overtime;
}
