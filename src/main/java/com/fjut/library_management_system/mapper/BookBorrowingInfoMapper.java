package com.fjut.library_management_system.mapper;

import com.fjut.library_management_system.entity.BookBorrowingInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fjut.library_management_system.vo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
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
public interface BookBorrowingInfoMapper extends BaseMapper<BookBorrowingInfo> {
    List<BorrowingVo> getAllBorrowingInfo(@Param("queryBorrowingVo") QueryBorrowingVo queryBorrowingVo,@Param("page") Long page);
    Long getBorrowingCount(@Param("queryBorrowingVo") QueryBorrowingVo queryBorrowingVo);
    List<BorrowingClassificationChartVo> getBookBorrowingClassificationInfo();

    List<BorrowingClassificationChartVo> getBorrowingClassificationInfoByUserId(Long userId,Integer year);

    List<BorrowingMonthChartVo> getBorrowingMonthInfoByUserId(Long userId, Integer year);

    List<HashMap<String,Object>> getDyingBorrowingBooks();

    List<HashMap<String,Object>> getOvertimeBorrowingBooks();

    HashMap<String,Object> getReturnBookInfo(@Param("userId") Long userId,@Param("callNumber") String callNumber);
}
