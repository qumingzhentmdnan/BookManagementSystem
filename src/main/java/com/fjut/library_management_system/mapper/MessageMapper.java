package com.fjut.library_management_system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fjut.library_management_system.entity.Message;

import java.util.List;

public interface MessageMapper extends BaseMapper<Message>{
    List<Message> selectMessageByUserId(Long userId,int page,int limit);

    int selectMessageCountByUserId(Long userId);
}
