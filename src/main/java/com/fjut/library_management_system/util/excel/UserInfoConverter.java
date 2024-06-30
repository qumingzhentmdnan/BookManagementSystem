package com.fjut.library_management_system.util.excel;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.converters.WriteConverterContext;
import com.alibaba.excel.metadata.data.WriteCellData;

//导出Excel转换器
public class UserInfoConverter implements Converter<Boolean> {

    //性别，将true转换为“男”，false转换为“女”
    @Override
    public WriteCellData<?> convertToExcelData(WriteConverterContext<Boolean> context) throws Exception {
        Boolean value = context.getValue();
        String convertedValue = Boolean.TRUE.equals(value) ? "男" : "女";
        return new WriteCellData<>(convertedValue);
    }
}