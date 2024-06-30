package com.fjut.library_management_system.util.excel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.fjut.library_management_system.controller.WebsocketController;
import com.fjut.library_management_system.entity.Message;
import com.fjut.library_management_system.exception.CustomizedException;
import com.fjut.library_management_system.util.Result;
import com.fjut.library_management_system.util.SecurityUtil;
import com.fjut.library_management_system.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.io.IOException;

//通过Excel导入数据时的异常处理
@Slf4j
public class ReadExcelHandle {
    //解析异常
    public static void onReadException(Exception exception, Object current) throws Exception {
        log.error("解析失败：{}", exception.getMessage());
        // 如果是某一个单元格的转换异常 能获取到具体行号
        // 如果要获取头的信息 配合invokeHeadMap使用
        if (exception instanceof ExcelDataConvertException excelDataConvertException) {
            //单行代码拼接，编译器会进行优化：String s = new StringBuilder().append("Hello, ").append("world!").toString();
            //多行代码拼接，编译器不会进行优化
            String res ="第" + (excelDataConvertException.getRowIndex()) + "行，第" +
                    (excelDataConvertException.getColumnIndex()) + "列解析异常，数据为:" +
                    excelDataConvertException.getCellData().getStringValue() +"\n\r已储存数据至："+
                    (current == null ? "未保存任何数据" :current);
            SpringContextUtil.getBean(WebsocketController.class).sendMessageToUser(new Message()
                    .setMessage(res).setFromUserId(0L).setToUserId(SecurityUtil.getCurrentUsername())
                    .setTitle("Excel读取"));
            throw new CustomizedException(20001,res);
        }else if(exception instanceof CustomizedException){
            throw exception;
        }
    }

    //校验异常
    public static void onValidationHasErrors(Errors errors, AnalysisContext context, Object current) throws IOException {
        // 如果校验不通过,抛出异常，拼接错误信息
        if (errors.hasErrors()) {
            StringBuilder alert = new StringBuilder().append("第")
                    .append(context.readRowHolder().getRowIndex())
                    .append("行数据校验失败：\n\r校验错误数据信息：");
            if (errors.hasErrors()) {
                for (ObjectError error : errors.getAllErrors()) {
                    if (error instanceof FieldError fieldError) {
                        alert.append(fieldError.getField()).append(":");
                    }
                    alert.append(error.getDefaultMessage()).append(";");
                }
            }
            String res = alert.append("\n\r已储存数据至：").append(current == null ? "未保存任何数据" : current).toString();
            System.out.println(SecurityUtil.getCurrentUsername());
            SpringContextUtil.getBean(WebsocketController.class).sendMessageToUser(new Message()
                    .setFromUserId(0L).setToUserId(SecurityUtil.getCurrentUsername())
                    .setTitle("Excel数据校验异常").setMessage(res));
            throw new CustomizedException(20001,res);
        }
    }

    //存储异常
    public static void onSavaException(Object current, Result result) throws IOException {
        String alert =result.getMessage()+"\n\r已储存数据至："+ (current == null ? "未保存数据" : "。保存至：" + current);
        SpringContextUtil.getBean(WebsocketController.class).sendMessageToUser(new Message()
                .setFromUserId(0L).setToUserId(SecurityUtil.getCurrentUsername())
                .setTitle("Excel数据存储异常").setMessage(alert));
        throw new CustomizedException(result.getCode(),alert);
    }
}