package com.fjut.library_management_system.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableField;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class AddBookVo implements Serializable {
    /**
     * 书籍id
     */
    @TableField("book_id")
    private Integer bookId;

    /**
     * 书籍名称
     */

    @ExcelProperty("书籍名称")
    @Pattern(regexp = "^.{0,150}$", message = "书名的长度必须在0到150个字符之间")
    @TableField("book_name")
    @NotNull(message = "书名不能为空")
    private String bookName;

    /**
     * isbn码
     */
    @ExcelProperty("isbn码")
    @Pattern(regexp = "^.{0,13}$", message = "isbn长度不符合规范，请输入10位或13位isbn码，无需分隔符")
    @NotNull(message = "isbn不能为空")
    private String isbn;

    /**
     * 分类码
     */
    @ExcelProperty("书籍分类码")
    @Pattern(regexp = "^.{0,20}$", message = "分类码的长度必须在0到20个字符之间")
    @NotNull(message = "分类码不能为空")
    private String classification;

    /**
     * 书籍作者
     */
    @ExcelProperty("书籍作者")
    @Pattern(regexp = "^.{0,100}$", message = "作者的长度必须在0到100个字符之间")
    @NotNull(message = "作者不能为空")
    private String author;

    /**
     * 出版社
     */
    @ExcelProperty("出版社")
    @Pattern(regexp = "^.{0,50}$", message = "出版社的长度必须在0到50个字符之间")
    @NotNull(message = "出版社不能为空")
    private String publisher;

    /**
     * 添加数量
     */
    @ExcelProperty("添加数量")
    @NotNull(message = "添加数量不能为空")
    @Min(message = "添加数量至少为1", value = 1)
    private Integer count;

    /**
     * 版本说明
     */
    @ExcelProperty("书籍版本说明")
    @Pattern(regexp = "^.{0,50}$", message = "版本说明的长度必须在0到50个字符之间")
    @NotNull(message = "版本说明不能为空")
    private String versionDescription;

    /**
     * 图书板式
     */
    @ExcelProperty("书籍板式")
    @Pattern(regexp = "^.{0,50}$", message = "图书板式的长度必须在0到50个字符之间")
    @NotNull(message = "图书板式不能为空")
    private String bookSize;

    /**
     * 价格
     */
    @ExcelProperty("书籍价格")
    @Min(value = 0, message = "价格不能为负数")
    @NotNull(message = "价格不能为空")
    private Double price;

    /**
     * 系列丛书
     */
    @ExcelProperty("系列丛书")
    @Pattern(regexp = "^.{0,150}$", message = "系列丛书说明的长度必须在0到150个字符之间")
    @NotNull(message = "系列丛书不能为空")
    private String series;

    /**
     * 说明
     */
    @ExcelProperty("相关说明")
    @Pattern(regexp = "^.{0,150}$", message = "说明的长度必须在0到150个字符之间")
    @NotNull(message = "说明不能为空")
    private String notes;

    /**
     * 摘要
     */
    @ExcelProperty("书籍摘要")
    @Pattern(regexp = "^.{0,255}$", message = "摘要的长度必须在0到255个字符之间")
    @NotNull(message = "摘要不能为空")
    private String abstractInfo;

    /**
     * 所属学科
     */
    @ExcelProperty("所属学科")
    @Pattern(regexp = "^.{0,50}$", message = "所属学科的长度必须在0到50个字符之间")
    @NotNull(message = "所属学科不能为空")
    private String subject;

    /**
     * 其他书名
     */
    @ExcelProperty("其他书名")
    @Pattern(regexp = "^.{0,50}$", message = "其他书名的长度必须在0到200个字符之间")
    @NotNull(message = "其他书名不能为空")
    private String parallelTitle;

    /**
     * 图书索书号
     * */
    @ExcelProperty("索书号")
    @NotNull(message = "索书号不能为空")
    private String callNumber;
}