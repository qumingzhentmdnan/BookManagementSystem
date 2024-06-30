package com.fjut.library_management_system.util.excel;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.converters.WriteConverterContext;
import com.alibaba.excel.metadata.data.WriteCellData;

//导出Excel转换器
public class BorrowingConverter {

    //是否归还状态，将true转换为“已归还”，false转换为“未归还”
    public static class ReturnedStatus implements Converter<Boolean> {
        @Override
        public WriteCellData<?> convertToExcelData(WriteConverterContext<Boolean> context) throws Exception {
            Boolean value = context.getValue();
            String convertedValue = Boolean.TRUE.equals(value) ? "已归还" : "未归还";
            return new WriteCellData<>(convertedValue);
        }
    }


    //是否超时状态，将true转换为“已超时”，false转换为“未超时”
    public static class OvertimeStatus implements Converter<Boolean> {
        @Override
        public WriteCellData<?> convertToExcelData(WriteConverterContext<Boolean> context) throws Exception {
            Boolean value = context.getValue();
            String convertedValue = Boolean.TRUE.equals(value) ? "已超时" : "未超时";
            return new WriteCellData<>(convertedValue);
        }
    }
}
