package com.fjut.library_management_system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fjut.library_management_system.entity.BookBorrowingInfo;
import com.fjut.library_management_system.entity.FineInfo;
import com.fjut.library_management_system.util.Result;
import com.fjut.library_management_system.vo.BorrowingClassificationChartVo;
import com.fjut.library_management_system.vo.BorrowingMonthChartVo;
import com.fjut.library_management_system.vo.BorrowingVo;
import com.fjut.library_management_system.vo.QueryBorrowingVo;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 叶良辰
 * @since 2024年03月31日
 */
public interface BookBorrowingInfoService extends IService<BookBorrowingInfo> {

    List<BorrowingVo> getAllBorrowingInfo(QueryBorrowingVo queryBorrowingVo);

    Long getCount(QueryBorrowingVo queryBorrowingVo);

    List<BorrowingClassificationChartVo> getBookBorrowingClassificationInfo();

    Map<String,Object> getUserCenterInfo(Long userId, Integer year);

    Result returnBook(Long userId, String isbn);

    Result borrowBook(Long userId, String callNumber);

    boolean queryUserHasFies(Long userId);

    boolean payFines(Long payId);
}
