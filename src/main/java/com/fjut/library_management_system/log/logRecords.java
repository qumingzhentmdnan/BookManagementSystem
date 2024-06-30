package com.fjut.library_management_system.log;

import com.fjut.library_management_system.util.Result;
import com.fjut.library_management_system.util.SecurityUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.hibernate.validator.constraints.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class logRecords {
    Logger logger = LoggerFactory.getLogger("com.fjut.operation");

    //Logger timeLogger=LoggerFactory.getLogger("com.fjut.timerecords");

    //注意，表达式中创建的切点方法，绝对不能包括WebsocketController中的方法，否则会@ServerEndPoint注解的类注册失败
    //Controller层的update、delete、add、remove方法
    @Pointcut("execution(* com.fjut.library_management_system.controller.*.update*(..))")
    public void update() {
    }

    @Pointcut("execution(* com.fjut.library_management_system.controller.*.delete*(..))")
    public void delete() {
    }

    @Pointcut("execution(* com.fjut.library_management_system.controller.*.add*(..))")
    public void add() {
    }

    @Pointcut("execution(* com.fjut.library_management_system.controller.*.remove*(..))")
    public void remove() {
    }

    //使用前置通知，对update、delete、add、remove等危险操作方法进行记录
    @After("update() || delete() || add()|| remove()")
    public void recordUpdateOperation(JoinPoint joinPoint) throws Throwable {
        //获取当前用户
        Long currentUsername = SecurityUtil.getCurrentUsername();
        String args;
        //获取方法名
        String methodName = joinPoint.getSignature().getName();
        if(methodName.equals("updateUserInfo")){
            HashMap<String, String> map=(HashMap<String, String>)joinPoint.getArgs()[0];
            map.put("password","********");
            args=map.toString();
        }else{
            args = Arrays.deepToString(joinPoint.getArgs());
        }

        logger.info("用户{}执行了{}方法，参数为{}", currentUsername, methodName, args);
        //2024-05-26 23:32:19.929 [http-nio-8080-exec-5] INFO  com.fjut.operation - 用户3221311414执行了updateBanUser方法，参数为[3000000000]
    }

//    @Around("execution(* com.fjut.library_management_system.controller..*(..)) && !execution(* com.fjut.library_management_system.controller.WebsocketController.*(..))")
//    @Order(1)
//    public Result time(ProceedingJoinPoint joinPoint) throws Throwable {
//        long start = System.currentTimeMillis();
//        Result result = (Result) joinPoint.proceed();
//        long end = System.currentTimeMillis();
//        timeLogger.info("方法{}执行时间为{}ms", joinPoint.getSignature().getName(), end - start);
//        return result;
//    }
}