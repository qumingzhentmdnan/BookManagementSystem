package com.fjut.library_management_system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fjut.library_management_system.entity.BookDetailInfo;
import com.fjut.library_management_system.entity.BookStoreInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BookDetailInfoMapper extends BaseMapper<BookDetailInfo> {
}
