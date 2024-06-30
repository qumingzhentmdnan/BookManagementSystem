package com.fjut.library_management_system.exception;


import com.alibaba.excel.exception.ExcelCommonException;
import com.fjut.library_management_system.util.Result;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Objects;

@RestControllerAdvice
@Slf4j
public class HandleException {

    //使用异步抛出的异常，会被CompletionException包装，无法直接捕获，会被全局异常捕获
    //如：java.util.concurrent.CompletionException: CustomizedException(code=20001, message=对于书籍5508000889，索书号TU126-78/568/12已存在，无法添加)



    //自定义异常处理
    @ExceptionHandler(CustomizedException.class)
    public Result HandleCustomizedException(CustomizedException exception){
        log.error(String.valueOf(exception));
        return Result.error().code(exception.getCode()).message(exception.getMessage());
    }

    //权限不足异常处理
    @ExceptionHandler(AccessDeniedException.class)
    public Result HandleAccessDeniedException(AccessDeniedException exception){
        log.error(String.valueOf(exception));
        return Result.error().message("您没有权限访问该资源");
    }

    //Excel文件格式错误异常处理
    @ExceptionHandler(ExcelCommonException.class)
    public Result HandleExcelCommonException(ExcelCommonException exception){
        log.error(String.valueOf(exception));
        return Result.error().message("Excel文件格式错误，请检查Excel文件格式是否正确");
    }

    //处理实体参数校验异常
    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class
            , BindException.class, TypeMismatchException.class, MissingServletRequestParameterException.class})
    public Result HandleMethodArgumentNotValidException(Exception exception){
        log.error(String.valueOf(exception));
        //异常匹配，进入捕获的异常
        if(exception instanceof MethodArgumentNotValidException)
            return Result.error().message(Objects.requireNonNull(((MethodArgumentNotValidException) exception).getBindingResult().getFieldError()).getDefaultMessage());
        if(exception instanceof BindException)
            return Result.error().message(Objects.requireNonNull(((BindException) exception).getBindingResult().getFieldError()).getDefaultMessage());
        if(exception instanceof TypeMismatchException)
            return Result.error().message("参数类型不匹配，请检查参数类型是否正确");
        if(exception instanceof MissingServletRequestParameterException)
            return Result.error().message("缺少请求参数，请检查请求参数是否正确");
        return Result.error().message(((ConstraintViolationException) exception).getConstraintViolations().iterator().next().getMessage());
    }

    //全局异常处理
    @ExceptionHandler(Exception.class)
    public Result GlobalHandle(Exception exception){
        log.error(String.valueOf(exception));
        return Result.error().message("您的网络存在异常，请稍后再试");
    }


}

