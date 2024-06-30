package com.fjut.library_management_system.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fjut.library_management_system.entity.Message;
import com.fjut.library_management_system.entity.User;
import com.fjut.library_management_system.mapper.MessageMapper;
import com.fjut.library_management_system.mapper.UserMapper;
import com.fjut.library_management_system.util.RedisUtil;
import com.fjut.library_management_system.util.SpringContextUtil;
import com.fjut.library_management_system.util.VirtualThreadUtil;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@ServerEndpoint(value = "/websocket/hasMessage")//主要是将目前的类定义成一个websocket服务器端, 注解的值将被用于监听用户连接的终端访问URL地址,客户端可以通过这个URL来连接到WebSocket服务器端
@Component
@Data
//创建WebsocketController之前先创建SpringContextUtil。默认情况下，Spring会使用类名的首字母小写作为bean的名称
@DependsOn("springContextUtil")
@Slf4j
public class WebsocketController {

    private  UserMapper userMapper=SpringContextUtil.getBean(UserMapper.class);

    private MessageMapper messageMapper=SpringContextUtil.getBean(MessageMapper.class);

    //存放访问用户的session
    private  static final HashMap<Long,Session> sessionMap = new HashMap<>();

    //websocket首次建立连接时调用
    @OnOpen
    public void onOpen(Session session, EndpointConfig config){
        try {
            // 获取URL中的用户ID参数
            Map<String, List<String>> params = session.getRequestParameterMap();
            List<String> userIds = params.get("userId");
            if(userIds==null|| userIds.isEmpty()){
                return;
            }
            //将用户ID和session绑定
            String currentUsername = userIds.getFirst();

            CompletableFuture<User> user = VirtualThreadUtil
                    .executorAsync(() -> userMapper.selectOne(new QueryWrapper<User>().select("has_message").eq("user_id", currentUsername)));

            sessionMap.put(Long.valueOf(currentUsername),session);

            //查询用户是否有未读消息,如果有，发出提醒
            if(user.join().isHasMessage()){
                remindUser(Long.valueOf(currentUsername));
            }
        } catch (IOException e) {
            log.error("websocket连接异常：",e);
        }
    }

    //客户端发送消息时调用
    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        //收到消息,进行心跳检测，保持连接
        if(message.equals("ping"))
            session.getBasicRemote().sendText("pong");
    }

    //链接发生错误时调用
    @OnError
    public void onError(Session session, Throwable error){
        sessionMap.entrySet().removeIf(entry -> entry.getValue().equals(session));
        log.error("websocket连接异常：",error);
    }

    //链接关闭时调用
    @OnClose
    public void onClose(Session session){
        //连接关闭时，将用户的session移除
        sessionMap.entrySet().removeIf(entry -> entry.getValue().equals(session));
    }

    //向用户发送消息
    public void sendMessageToUser(Message message){
        //消息长度限制
        if(message.getTitle().length()>50){
            message.setTitle(message.getTitle().substring(0,50));
        }
        if(message.getMessage().length()>255){
            message.setMessage(message.getMessage().substring(0,253)+"……");
        }

        //清除缓存
        if(message.getToUserId()==0){
            VirtualThreadUtil.executorAsync(()->new RedisUtil().removeCacheByPrefix("userMessage::"));
        }else{
            VirtualThreadUtil.executorAsync(()->new RedisUtil().removeCacheByPrefix("userMessage::"+message.getToUserId()));
        }

        //插入消息
        VirtualThreadUtil.executor(() -> messageMapper.insert(message));

        try {
            //向用户发送提醒
            remindUser(message.getToUserId());
        } catch (IOException e) {
            log.error("向{}发送信息:{}失败",message.getToUserId(),message.getMessage(),e);
        }
    }

    //提醒用户有未读消息
    public void remindUser(Long toUserId) throws IOException {
        //更新数据库,0发送给所有用户，否则发送给指定用户
        VirtualThreadUtil
                .executorAsync(()->userMapper.update(new UpdateWrapper<User>().set("has_message",true).eq(toUserId!= 0L,"user_id",toUserId)));

        //系统消息，发送给所有在线用户
        if(toUserId==0L) {
            sessionMap.values().forEach(session -> {
                try {
                    session.getBasicRemote().sendText("yes");
                } catch (IOException e) {
                    log.error("向用户{}发送信息提醒失败",toUserId,e);
                }
            });
        }else{
            //发送给指定用户,如果用户在线的话
            Session session = sessionMap.get(toUserId);
            if(session!=null){
                session.getBasicRemote().sendText("yes");
            }
        }

    }

    //移除提醒,在测回消息是使用
    public void withdrawRemind(Long toUserId) throws IOException {
        //更新数据库
        VirtualThreadUtil
                .executorAsync(()->userMapper.update(new UpdateWrapper<User>().set("has_message",false).eq("user_id",toUserId)));

        //发送给指定用户
        Session session = sessionMap.get(toUserId);
        if(session!=null){
            session.getBasicRemote().sendText("no");
        }
    }
}