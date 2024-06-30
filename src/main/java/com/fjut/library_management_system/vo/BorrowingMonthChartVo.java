package com.fjut.library_management_system.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class BorrowingMonthChartVo implements Serializable {
    private Integer month;
    private Integer count;
}