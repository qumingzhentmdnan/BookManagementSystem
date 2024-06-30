package com.fjut.library_management_system.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
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
@ToString
@TableName("book")
public class BookInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 书籍id
     */
    @TableId(value = "book_id", type = IdType.AUTO)
    private Integer bookId;

    /**
     * 书籍名称
     */
    @TableField("book_name")
    private String bookName;

    /**
     * isbn码
     */
    @TableField("isbn")
    private String isbn;

    /**
     * 书籍位置
     */
    @TableField("classification")
    private String classification;

    /**
     * 书籍作者
     */
    @TableField("author")
    private String author;

    /**
     * 出版社
     */
    @TableField("publisher")
    private String publisher;

    /**
     * 总数
     */
    @TableField("total")
    private Integer total;

    /**
     * 剩余数量
     */
    @TableField("remain")
    private Integer remain;

    /**
     * 是否删除
     */
    @TableField("is_deleted")
    @TableLogic
    private Boolean deleted;
}
