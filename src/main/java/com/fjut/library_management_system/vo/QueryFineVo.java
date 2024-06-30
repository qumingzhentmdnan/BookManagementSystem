package com.fjut.library_management_system.vo;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Range;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class QueryFineVo implements Serializable {
    @Range(min=1000000000, max=9999999999L, message = "请输入正确的10位身份凭证")
    private Long userId;

    @Pattern(regexp = "^.{0,150}$", message = "书名的长度必须在0到150个字符之间")
    private String bookName;

    @Pattern(regexp = "^.{0,20}$", message = "用户名的长度必须在0到20个字符之间")
    private String userName;

    private Boolean payed;

    private LocalDate payingDate;

    @Min(value = 1, message = "当前页不能为负数")
    @NotNull(message = "当前页数不能为空")
    private Integer page;


    @Min(value = 1, message = "分页行数必须大于等于1")
    @Max(value = 100, message = "分页行数不超过100")
    @NotNull(message = "分页行数不能为空")
    private Integer limit;
}