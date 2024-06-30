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
@ToString
@Accessors(chain = true)
@TableName("book_detail_info")
public class BookDetailInfo implements Serializable {

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
     * 版本说明
     */
    @TableField("version_description")
    private String versionDescription;

    /**
     * 图书板式
     */
    @TableField("book_size")
    private String bookSize;

    /**
     * 价格
     */
    @TableField("price")
    private Double price;

    /**
     * 系列丛书
     */
    @TableField("series")
    private String series;

    /**
     * 说明
     */
    @TableField("notes")
    private String notes;

    /**
     * 摘要
     */
    @TableField("abstract_info")
    private String abstractInfo;

    /**
     * 所属学科
     */
    @TableField("subject")
    private String subject;

    /**
     * 其他书名
     */
    @TableField("parallel_title")
    private String parallelTitle;

    /**
     * 是否删除
     */
    @TableField("is_deleted")
    @TableLogic
    private Boolean deleted;
}
