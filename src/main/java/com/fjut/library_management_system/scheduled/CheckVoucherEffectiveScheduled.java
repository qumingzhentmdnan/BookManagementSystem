package com.fjut.library_management_system.scheduled;

import com.fjut.library_management_system.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.autoconfigure.data.ConditionalOnRepositoryType;
import org.springframework.boot.autoconfigure.graphql.ConditionalOnGraphQlSchema;
import org.springframework.boot.autoconfigure.security.ConditionalOnDefaultWebSecurity;
import org.springframework.boot.autoconfigure.web.ConditionalOnEnabledResourceChain;
import org.springframework.boot.autoconfigure.web.servlet.ConditionalOnMissingFilterBean;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

//定时任务，每天0点检查所有未生效的凭证，如果已过期，则设置账号状态为凭证已过期
@Component
public class CheckVoucherEffectiveScheduled {
    @Autowired
    private UserMapper userMapper;

    // 每天0点检查所有未生效的凭证，如果已过期，则设置账号状态为凭证已过期
    @Scheduled(cron = "0 0 0 * * ?")
    public void checkVoucherEffective() {
        userMapper.checkVoucherEffective();
    }
}

