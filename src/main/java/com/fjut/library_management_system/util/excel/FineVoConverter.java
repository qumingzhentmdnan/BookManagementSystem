package com.fjut.library_management_system.util.excel;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.converters.WriteConverterContext;
import com.alibaba.excel.metadata.data.WriteCellData;

//导出Excel转换器
public class FineVoConverter implements Converter<Boolean> {

    //是否支付状态，将true转换为“已支付”，false转换为“未支付”
    @Override
    public WriteCellData<?> convertToExcelData(WriteConverterContext<Boolean> context) throws Exception {
        Boolean value = context.getValue();
        String convertedValue = Boolean.TRUE.equals(value) ? "已支付" : "未支付";
        return new WriteCellData<>(convertedValue);
    }
}