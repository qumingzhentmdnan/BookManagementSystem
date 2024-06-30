package com.fjut.library_management_system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fjut.library_management_system.entity.Message;
import com.fjut.library_management_system.entity.User;
import com.fjut.library_management_system.entity.UserIdentity;
import com.fjut.library_management_system.util.Result;
import com.fjut.library_management_system.vo.AddUserVo;
import com.fjut.library_management_system.vo.QueryUserVo;
import com.fjut.library_management_system.vo.UserBorrowingChartVo;
import com.fjut.library_management_system.vo.UserInfoVo;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 叶良辰
 * @since 2024年03月31日
 */
public interface UserService {

    Page<User> getUserInfo(QueryUserVo queryUserVo);

    UserInfoVo getUserDetailInfo(Long userId);

    boolean updateStatus(Long userId);

     List<Long> getAllUserId();

     List<UserInfoVo> getAllUserDetailInfo(QueryUserVo queryUserVo);

    List<UserBorrowingChartVo> getUserBorrowingSortInfo();

    Result getGlobalUserInfo(Long userId);

    boolean updateAvatar(String avatar,Long userId);

    List<UserIdentity> getAllIdentity();

    Result addUser(List<AddUserVo> addUserVo);

    boolean deleteUser(Long userId);

    boolean extendUserVoucher(LocalDate date,Long userId);

    boolean queryUserHadPhone(Long userId);

    boolean queryPhoneHasUser(Long phone);
    boolean updateUserInfo(String phone, String password,Long userId);

    Result getUserMessage(Long userId,int page,int limit);

    List<Message> getSendMessage();

    boolean deleteSendMessage(Long messageId);

    Result sendMessage(Long userId,String title, String message);
}
