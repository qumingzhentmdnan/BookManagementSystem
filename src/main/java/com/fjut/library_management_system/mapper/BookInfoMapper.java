package com.fjut.library_management_system.mapper;

import com.fjut.library_management_system.entity.BookDetailInfo;
import com.fjut.library_management_system.entity.BookInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fjut.library_management_system.vo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
public interface BookInfoMapper extends BaseMapper<BookInfo> {
    BookInfoVo getBookDetailInfo(Integer bookId,String isbn) ;

    List<BookInfoVo> getAllBookDetailInfo(@Param("queryBook") QueryBookVo queryBook);

    List<BookChartVo> getBookChartInfo();

    int insertBookDetailInfosBatch(List<BookDetailInfo> bookDetailInfos);
}
