package com.fjut.library_management_system.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
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
@TableName("book_store_info")
public class BookStoreInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 图书id
     */
    @TableField("book_id")
    private Integer bookId;



    /**
     * 索书号
     */
    @TableField("call_number")
    private String callNumber;

    /**
     * 书籍借出时间
     */
    @TableField("borrowing_date")
    private LocalDateTime borrowingDate;

    /**
     * 书籍归还时间
     */
    @TableField("return_date")
    private LocalDateTime returnDate;

    /**
     * 是否借出
     */
    @TableField("is_borrowing")
    private Boolean borrowing;

    /**
     * 是否删除
     */
    @TableField("is_deleted")
    @TableLogic
    private Boolean deleted;
}
