package com.fjut.library_management_system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fjut.library_management_system.entity.BookInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fjut.library_management_system.entity.BookStoreInfo;
import com.fjut.library_management_system.mapper.BookDetailInfoMapper;
import com.fjut.library_management_system.mapper.BookStoreInfoMapper;
import com.fjut.library_management_system.util.Result;
import com.fjut.library_management_system.vo.*;
import org.apache.commons.math3.analysis.function.Add;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 叶良辰
 * @since 2024年03月31日
 */
public interface BookInfoService {
    Page<BookInfo> getBookInfo(QueryBookVo queryBookVo);

    BookInfoVo getBookDetailInfo(Integer bookId);

    boolean updateBookInfo(BookInfoVo bookInfoVo);

    boolean deleteBookInfo(Integer bookId);

    Result addBookInfo(List<AddBookVo> bookInfos);

    BookInfoVo getBookByIsbn(String isbn);

    List<BookStoreInfo> getCallNumber(Integer bookId);

    boolean deleteCallNumber(String callNumber,Integer bookId);

    List<Integer> getAllBookId();

    List<BookInfoVo> getAllBookDetailInfo(QueryBookVo queryBookVo);

    Result getCountInfo();

    List<BookChartVo> getBookChartInfo();
}
