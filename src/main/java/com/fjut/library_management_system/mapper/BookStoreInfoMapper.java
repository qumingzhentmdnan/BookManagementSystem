package com.fjut.library_management_system.mapper;

import com.fjut.library_management_system.entity.BookStoreInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author 叶良辰
 * @since 2024年03月31日
 */
@Mapper
public interface BookStoreInfoMapper extends BaseMapper<BookStoreInfo> {
    int insertBookStoreInfosBatch(List<BookStoreInfo> bookStoreInfos);
}
