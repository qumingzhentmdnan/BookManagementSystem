package com.fjut.library_management_system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fjut.library_management_system.entity.UserDetailInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserDetailMapper extends BaseMapper<UserDetailInfo> {
    int insertBatch(List<UserDetailInfo> userDetailInfos);
}
