package com.fjut.library_management_system.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class UserBorrowingChartVo implements Serializable {
    private String userName;
    private Integer count;
}