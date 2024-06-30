package com.fjut.library_management_system.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class ExportBookExcelVo implements Serializable {
    private QueryBookVo queryBookVo;
    private List<String> exportList;
}