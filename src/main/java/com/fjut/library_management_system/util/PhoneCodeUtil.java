package com.fjut.library_management_system.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

//调用短信接口
public class PhoneCodeUtil {


//Component是把普通类在spring加载时候加载成bean，这时候 redisTemplate 还未加载，自动注入的是空指针了
//    @Autowired
//    private RedisTemplate redisTemplate;
    //获取验证码
    public static  Result getCode(Long userName,String phone,RedisTemplate<String,Object> redisTemplate) {
        String resCode = userName+":"+phone + ":code";
        String resCount = userName + ":"+phone +":count";

        //判断是否需要发送验证码
        if (Boolean.TRUE.equals(redisTemplate.hasKey(resCode))) {
            return Result.error().message("验证码已发送，请稍后再试");
        }
        //判断是否发送次数已达上限
        if (Boolean.TRUE.equals(redisTemplate.hasKey(resCount))) {
            Object o = redisTemplate.opsForValue().get(resCount);
            long count;
            if(o==null){
                count=0;
            }else{
                count=Long.parseLong(o.toString());
            }
            if (count >= 1) {
                return Result.error().message("今日发送次数已达上限,请24小时后再试");
            }
        }

        //获取验证码
        Random random = new Random();
        int code = random.nextInt(1000, 9999);

        //请求信息
        String host = "https://gyytz.market.alicloudapi.com";
        String path = "/sms/smsSend";
        String method = "POST";
        String appcode = "d754ad9052544cffade21ff748aa096f";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("mobile", phone);
        querys.put("param", "**code**:"+code+",**minute**:10");
        querys.put("smsSignId", "2e65b1bb3d054466b82f0c9d125465e2");
        querys.put("templateId", "908e94ccf08b4476ba6c876d13f084ad");
        Map<String, String> bodys = new HashMap<String, String>();
        try {
            /**
             * 重要提示如下:
             * HttpUtils请从
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
             * 下载
             *
             * 相应的依赖请参照
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
             */
            HttpResponse response = VirtualThreadUtil
                    .executor(()-> {
                        try {
                            return HttpUtils.doPost(host, path, method, headers, querys, bodys);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });

            HttpEntity entity = response.getEntity();
            if (entity != null) {
                //将response的body转为字符串
                String result = EntityUtils.toString(entity, "UTF-8");
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> map = mapper.readValue(result, Map.class);
                //判断是否发送成功
                if(map.get("msg").equals("成功")){
                    //将验证码存入redis
                    VirtualThreadUtil
                            .executor(()->redisTemplate.opsForValue().set(resCode, String.valueOf(code), 10, TimeUnit.MINUTES));

                    //设置用户发送验证码次数
                    if (Boolean.FALSE.equals(redisTemplate.hasKey(resCount))) {
                        //如果没有该键，设置为1
                        VirtualThreadUtil.executor(()->redisTemplate.opsForValue().set(resCount, "1",1, TimeUnit.DAYS));
                    } else
                        //如果有该键，次数加1
                        VirtualThreadUtil.executor(()->redisTemplate.opsForValue().increment(resCount, 1));
                    return Result.ok().message("验证码发送成功");
                }
                else{
                    System.out.println(map.get("status")+"  "+map.get("reason"));
                    return Result.error().message("验证码发送失败");
                }
            }else{
                return Result.error().message("验证码发送失败");
            }
            //获取response的body
            //System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.error().message("验证码发送成功");
    }

    public static void main(String[] args) {
//        //请求信息
//        //请求信息
//        String host = "https://gyytz.market.alicloudapi.com";
//        String path = "/sms/smsSend";
//        String method = "POST";
//        String appcode = "d754ad9052544cffade21ff748aa096f";
//        Map<String, String> headers = new HashMap<String, String>();
//        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
//        headers.put("Authorization", "APPCODE " + appcode);
//        Map<String, String> querys = new HashMap<String, String>();
//        querys.put("mobile", "18250956975");
//        querys.put("param", "**code**:"+"6666"+",**minute**:10");
//        querys.put("smsSignId", "2e65b1bb3d054466b82f0c9d125465e2");
//        querys.put("templateId", "908e94ccf08b4476ba6c876d13f084ad");
//        Map<String, String> bodys = new HashMap<String, String>();
//        try {
//            HttpResponse httpResponse = HttpUtils.doPost(host, path, method, headers, querys, bodys);
//            System.out.println(httpResponse);
//            System.out.println(httpResponse.getEntity());
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
    }
}