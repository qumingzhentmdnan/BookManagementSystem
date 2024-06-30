package com.fjut.library_management_system.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

//自定义异常类
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomizedException extends RuntimeException{
    private Integer code;//状态码;
    private String message;//相应消息
}