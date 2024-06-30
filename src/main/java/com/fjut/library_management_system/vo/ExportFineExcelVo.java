package com.fjut.library_management_system.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class ExportFineExcelVo implements Serializable {
    private QueryFineVo queryFineVo;
    private List<String> exportList;
}