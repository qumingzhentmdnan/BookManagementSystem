package com.fjut.library_management_system.util;

import org.apache.poi.ss.formula.functions.T;

import java.util.concurrent.*;
import java.util.function.Supplier;

public class VirtualThreadUtil {

    //创建一个虚拟线程池
    public static final ExecutorService virtualThreadPool = Executors.newVirtualThreadPerTaskExecutor();

    //执行一个异步任务，countDownLatch.countDown()，并返回一个CompletableFuture
    public static <T> CompletableFuture<T> executorAsync(Supplier<T> func,CountDownLatch countDownLatch){
        return CompletableFuture.supplyAsync(()->{
            T t = func.get();
            countDownLatch.countDown();
            return t;
        },virtualThreadPool);
    }

    //接受Supplier作为参数，执行一个异步任务，并返回一个CompletableFuture
    public static <T> CompletableFuture<T> executorAsync(Supplier<T> func){
        return CompletableFuture.supplyAsync(func,virtualThreadPool);
    }

    //接受Runnable作为参数，执行一个异步任务，并返回一个Thread
    public static Thread executorAsync(Runnable func){
        return Thread.startVirtualThread(func);
    }

    //开启虚拟线程执行一个任务，并立刻返回执行结果
    public static <T> T executor(Supplier<T> func){
       return CompletableFuture.supplyAsync(func,virtualThreadPool).join();
    }

    //开启虚拟线程执行一个任务，并立刻返回执行结果
    public static void executor(Runnable func){
        try {
            Thread.startVirtualThread(func).join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}