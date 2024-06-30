package com.fjut.library_management_system.mapper;

import com.fjut.library_management_system.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fjut.library_management_system.vo.QueryUserVo;
import com.fjut.library_management_system.vo.UserBorrowingChartVo;
import com.fjut.library_management_system.vo.UserInfoVo;
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
public interface UserMapper extends BaseMapper<User> {
     UserInfoVo getUserDetailInfo(Long userId);

     List<UserInfoVo> getAllUserDetailInfo(@Param("queryUser") QueryUserVo queryUserVo);
     List<UserBorrowingChartVo> getUserBorrowingSortInfo();

     int insertBatch(List<User> users);

     void checkVoucherEffective();
}
