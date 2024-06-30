package com.fjut.library_management_system.util;


import io.jsonwebtoken.Jwts;
import javax.crypto.SecretKey;
import java.util.Date;



//jwt工具类
public class JwtUtil{
    //设置token过期时间
    private  final  static long tokenExpiration = 60*60*1000;
    //new一个key,类共享
    private final static SecretKey key = Jwts.SIG.HS256.key().build();

    //将用户名转为token
    public static String createToken(String username){
        return Jwts.builder().
                subject(username).//设置用户名
                signWith(key).//设置加密key
                expiration(new Date(System.currentTimeMillis()+tokenExpiration)).//设置过期时间,30分钟
                compressWith(Jwts.ZIP.GZIP).//设置加密方式
                compact();
    }

    //通过token获取userName
    public static String getUsername(String jws){
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(jws).getPayload().getSubject();
    }

    //验证token是否有效
    public static boolean isTokenValid(String jws){
        try{
            Jwts.parser().verifyWith(key).build().parseSignedClaims(jws);
            return true;
        }catch (Exception e){
            return false;
        }
    }
}


