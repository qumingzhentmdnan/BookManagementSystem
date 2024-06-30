package com.fjut.library_management_system.controller;

import com.fjut.library_management_system.service.UserService;
import com.fjut.library_management_system.util.Result;
import com.fjut.library_management_system.util.SecurityUtil;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Optional;

@RestController
@RequestMapping("/message")
public class MessageController {
    private final UserService userService;

    @Autowired
    public MessageController(UserService userService) {
        this.userService = userService;
    }
    /**
     * 得到用户的信息
     * */
    //加入缓存，key为当前用户的id+page+limit
    @Cacheable(value = "userMessage", key = "T(com.fjut.library_management_system.util.SecurityUtil).getCurrentUsername()+'-'+#page+'-'+#limit")
    @GetMapping("/getUserMessage/{page}/{limit}")
    public Result getUserMessage(@Min(value = 1)@PathVariable("page") int page,
                                 @Min(value = 1) @Max(50) @PathVariable("limit") int limit){
        Long userId = SecurityUtil.getCurrentUsername();
        return userService.getUserMessage(userId,(page - 1) * limit,limit);
    }

    /**
     * 得到所有管理员发送的信息
     * */
    @GetMapping("/getSendMessage")
    public Result getSendMessage(){
        return Result.ok().data("userInfo",userService.getSendMessage());
    }

    /**
     * 撤回管理员发送的信息
     * */
    @CacheEvict(value = "userMessage", allEntries = true)
    @DeleteMapping("/deleteSendMessage")
    public Result deleteSendMessage(@Digits(integer = 18, fraction = 0, message = "消息id必须为数字")
                                    Long messageId){
        boolean res = userService.deleteSendMessage(messageId);
        return res?Result.ok().message("删除成功"):Result.error().message("删除失败");
    }

    /**
     * 向所有用户发送信息
     * */
    @PostMapping("/sendMessage")
    public Result sendMessage(@RequestBody HashMap<String,String> hashMap){
        String title = hashMap.get("title");
        String message = hashMap.get("message");
        if(title.isEmpty()||message.isEmpty()){
            return Result.error().message("标题和内容不能为空");
        }
        if(title.length()>50){
            return Result.error().message("标题长度不能超过50");
        }
        if(message.length()>255){
            return Result.error().message("内容长度不能超过255");
        }
        Long userId = SecurityUtil.getCurrentUsername();
        return userService.sendMessage(userId,title,message);
    }
}