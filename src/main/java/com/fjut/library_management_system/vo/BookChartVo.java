package com.fjut.library_management_system.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class BookChartVo implements Serializable {
    private String bookName;
    private Integer bookId;
    private String isbn;
    private String author;
    private String publisher;
    private Integer count;
}