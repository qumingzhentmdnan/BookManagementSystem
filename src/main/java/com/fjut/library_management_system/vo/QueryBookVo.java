package com.fjut.library_management_system.vo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

//查询接口对象类
@Data
@ToString
@EqualsAndHashCode(callSuper = false)
public class QueryBookVo implements Serializable {
    //数据格式验证，仅对非空数据进行验证
    @Pattern(regexp = "^.{0,150}$", message = "书名的长度必须在0到150个字符之间")
    private String bookName;

    @Pattern(regexp = "^.{0,100}$", message = "作者的长度必须在0到100个字符之间")
    private String author;

    @Pattern(regexp = "^.{0,20}$", message = "分类码的长度必须在0到20个字符之间")
    private String classification;

    @Pattern(regexp = "^.{0,13}$", message = "isbn长度不符合规范，请输入10位或13位isbn码，无需分隔符")
    private String isbn;

    @Pattern(regexp = "^.{0,50}$", message = "出版社的长度必须在0到50个字符之间")
    private String publisher;

    @Min(value = 1, message = "当前页不能为负数")
    @NotNull(message = "当前页数不能为空")
    private Integer page;


    @Min(value = 1, message = "分页行数必须大于等于1")
    @Max(value = 100, message = "分页行数不超过100")
    @NotNull(message = "分页行数不能为空")
    private Integer limit;
}