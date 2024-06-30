package com.fjut.library_management_system.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.baomidou.mybatisplus.annotation.TableField;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
@EqualsAndHashCode(callSuper = false)
public class BookInfoVo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 书籍id
     */
    @TableField("book_id")
    @ExcelIgnore
    private Integer bookId;

    /**
     * 书籍名称
     */
    @ExcelProperty("书籍名称")
    @ColumnWidth(40)
    @Pattern(regexp = "^.{0,150}$", message = "书名的长度必须在0到150个字符之间")
    @TableField("book_name")
    @NotNull(message = "书名不能为空")
    private String bookName;

    /**
     * isbn码
     */
    @ExcelProperty("isbn码")
    @ColumnWidth(20)
    @Pattern(regexp = "^.{0,13}$", message = "isbn长度不符合规范，请输入10位或13位isbn码，无需分隔符")
    @TableField("isbn")
    @NotNull(message = "isbn不能为空")
    private String isbn;

    /**
     * 分类码
     */
    @ExcelProperty("书籍分类码")
    @ColumnWidth(20)
    @Pattern(regexp = "^.{0,20}$", message = "分类码的长度必须在0到20个字符之间")
    @TableField("classification")
    @NotNull(message = "分类码不能为空")
    private String classification;

    /**
     * 书籍作者
     */
    @ExcelProperty("书籍作者")
    @ColumnWidth(20)
    @Pattern(regexp = "^.{0,100}$", message = "作者的长度必须在0到100个字符之间")
    @TableField("author")
    @NotNull(message = "作者不能为空")
    private String author;

    /**
     * 出版社
     */
    @ExcelProperty("出版社")
    @ColumnWidth(20)
    @Pattern(regexp = "^.{0,50}$", message = "出版社的长度必须在0到50个字符之间")
    @TableField("publisher")
    @NotNull(message = "出版社不能为空")
    private String publisher;

    /**
     * 总数
     */
    @ExcelProperty("书籍总数")
    @ColumnWidth(20)
    @TableField("total")
    @NotNull(message = "总数不能为空")
    private Integer total;

    /**
     * 剩余数量
     */
    @ExcelProperty("未借阅书籍数量")
    @ColumnWidth(20)
    @TableField("remain")
    private Integer remain;


    /**
     * 版本说明
     */
    @ExcelProperty("书籍版本说明")
    @ColumnWidth(20)
    @Pattern(regexp = "^.{0,50}$", message = "版本说明的长度必须在0到50个字符之间")
    @TableField("version_description")
    @NotNull(message = "版本说明不能为空")
    private String versionDescription;

    /**
     * 图书板式
     */
    @ExcelProperty("书籍板式")
    @ColumnWidth(20)
    @Pattern(regexp = "^.{0,50}$", message = "图书板式的长度必须在0到50个字符之间")
    @TableField("book_size")
    @NotNull(message = "图书板式不能为空")
    private String bookSize;

    /**
     * 价格
     */
    @ExcelProperty("书籍价格")
    @ColumnWidth(20)
    @Min(value = 0, message = "价格不能为负数")
    @TableField("price")
    @NotNull(message = "价格不能为空")
    private Double price;

    /**
     * 系列丛书
     */
    @ExcelProperty("系列丛书")
    @ColumnWidth(20)
    @Pattern(regexp = "^.{0,150}$", message = "系列丛书说明的长度必须在0到150个字符之间")
    @TableField("series")
    @NotNull(message = "系列丛书不能为空")
    private String series;

    /**
     * 说明
     */
    @ExcelProperty("相关说明")
    @ColumnWidth(40)
    @Pattern(regexp = "^.{0,150}$", message = "说明的长度必须在0到150个字符之间")
    @TableField("notes")
    @NotNull(message = "说明不能为空")
    private String notes;

    /**
     * 摘要
     */
    @ExcelProperty("书籍摘要")
    @ColumnWidth(40)
    @Pattern(regexp = "^.{0,255}$", message = "摘要的长度必须在0到255个字符之间")
    @TableField("abstract_info")
    @NotNull(message = "摘要不能为空")
    private String abstractInfo;

    /**
     * 所属学科
     */
    @ExcelProperty("所属学科")
    @ColumnWidth(20)
    @Pattern(regexp = "^.{0,50}$", message = "所属学科的长度必须在0到50个字符之间")
    @TableField("subject")
    @NotNull(message = "所属学科不能为空")
    private String subject;

    /**
     * 其他书名
     */
    @ExcelProperty("其他书名")
    @ColumnWidth(20)
    @Pattern(regexp = "^.{0,50}$", message = "其他书名的长度必须在0到200个字符之间")
    @TableField("parallel_title")
    @NotNull(message = "其他书名不能为空")
    private String parallelTitle;

    /**
     * 书籍存放位置
     */
    @ExcelProperty("索书号")
    @ColumnWidth(40)
    private String bookStore;
}