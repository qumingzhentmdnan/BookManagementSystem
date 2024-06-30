package com.fjut.library_management_system.util;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;

@Component
public class GetSqlConnection {


    //多线程下， @Transactional无法回滚子线程数据，所以需要手动获取数据库连接，通过一个sqlSession来获取
    public static Connection getSqlConnection() {
        // 获取数据库连接,获取会话(内部自有事务)
        SqlSessionFactory sqlSessionFactory = SpringContextUtil.getBean(SqlSessionFactory.class);
        SqlSession sqlSession = sqlSessionFactory.openSession();
        return sqlSession.getConnection();
    }
}