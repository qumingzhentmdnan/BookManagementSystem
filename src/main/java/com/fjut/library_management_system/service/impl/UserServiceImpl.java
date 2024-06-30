package com.fjut.library_management_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fjut.library_management_system.controller.WebsocketController;
import com.fjut.library_management_system.entity.Message;
import com.fjut.library_management_system.entity.User;
import com.fjut.library_management_system.entity.UserDetailInfo;
import com.fjut.library_management_system.entity.UserIdentity;
import com.fjut.library_management_system.exception.CustomizedException;
import com.fjut.library_management_system.mapper.*;
import com.fjut.library_management_system.service.UserService;
import com.fjut.library_management_system.util.Result;
import com.fjut.library_management_system.util.SpringContextUtil;
import com.fjut.library_management_system.util.VirtualThreadUtil;
import com.fjut.library_management_system.vo.AddUserVo;
import com.fjut.library_management_system.vo.QueryUserVo;
import com.fjut.library_management_system.vo.UserBorrowingChartVo;
import com.fjut.library_management_system.vo.UserInfoVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 叶良辰
 * @since 2024年03月31日
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {


    private final UserMapper userMapper;


    private final UserPermissionMapper userPermissionMapper;


    private final UserDetailMapper userDetailMapper;


    private final UserIdentityMapper userIdentityMapper;


    private final MessageMapper messageMapper;

    @Autowired
    public UserServiceImpl(UserMapper userMapper, UserPermissionMapper userPermissionMapper, UserDetailMapper userDetailMapper, UserIdentityMapper userIdentityMapper, MessageMapper messageMapper) {
        this.userMapper = userMapper;
        this.userPermissionMapper = userPermissionMapper;
        this.userDetailMapper = userDetailMapper;
        this.userIdentityMapper = userIdentityMapper;
        this.messageMapper = messageMapper;
    }

    //根据条件查询用户信息，分页
    @Override
    public Page<User> getUserInfo(QueryUserVo queryUserVo) {
        QueryWrapper<User> userQuery = new QueryWrapper<User>()
                .select("user_id", "user_name", "department", "phone", "identity","ban","expire")
                .eq(queryUserVo.getUserId() != null, "user_id", queryUserVo.getUserId())
                .like(queryUserVo.getUserName() != null && !queryUserVo.getUserName().isEmpty(), "user_name", queryUserVo.getUserName())
                .like(queryUserVo.getUserDepartment() != null && !queryUserVo.getUserDepartment().isEmpty(), "department", queryUserVo.getUserDepartment())
                .eq(queryUserVo.getUserPhone() != null, "phone", queryUserVo.getUserPhone())
                .eq(queryUserVo.getUserIdentity() != null && !queryUserVo.getUserIdentity().isEmpty(), "identity", queryUserVo.getUserIdentity())
                .eq("is_deleted", 0);

        return VirtualThreadUtil.executor(()->userMapper.selectPage(new Page<User>().setSize(queryUserVo.getLimit())
                .setCurrent(queryUserVo.getPage()), userQuery));
    }

    //根据用户id查询用户详细信息
    @Override
    public UserInfoVo getUserDetailInfo(Long userId) {
        return VirtualThreadUtil.executor(()->userMapper.getUserDetailInfo(userId));
    }

    //更新用户状态,禁用或解禁
    @Override
    public boolean updateStatus(Long userId) {
        return VirtualThreadUtil.executor(()->userMapper.update(new UpdateWrapper<User>().setSql("ban=not ban").eq("user_id",userId))>0);
    }

    //得到所有用户id
    @Override
    public List<Long> getAllUserId(){
        List<User> users = VirtualThreadUtil.executor(()->userMapper.selectList(new QueryWrapper<User>().select("user_id")));
        return users.stream().map(User::getUserId).collect(Collectors.toList());
    }

    //更新用户头像
    @Override
    public boolean updateAvatar(String avatar, Long userId) {
        return VirtualThreadUtil.executor(()->userDetailMapper.update(new UpdateWrapper<UserDetailInfo>().set("avatar",avatar).eq("user_id",userId))>0);
    }

    //得到用户基本信息和详细信息
    @Override
    public List<UserInfoVo> getAllUserDetailInfo(QueryUserVo queryUserVo) {
        return VirtualThreadUtil.executor(()->userMapper.getAllUserDetailInfo(queryUserVo));
    }

    //得到用户借阅排行榜
    @Override
    public List<UserBorrowingChartVo> getUserBorrowingSortInfo() {
        return VirtualThreadUtil.executor(()->userMapper.getUserBorrowingSortInfo());
    }

    //得到全局的用户信息
    @Override
    public Result getGlobalUserInfo(Long userId) {
        CompletableFuture<List<String>> res = VirtualThreadUtil.executorAsync(() -> userPermissionMapper.getPermissionList(userId));
        UserInfoVo userDetailInfo = getUserDetailInfo(userId);

        List<String> roles = res.join();

        //如果用户没有任何权限，设置为none
        if(roles==null||roles.isEmpty()){
            roles=List.of("none");
        }

        return Result.ok().data("roles", roles)
                .data("avatar", userDetailInfo.getAvatar())
                .data("name", userDetailInfo.getUserId())
                .data("introduction", userDetailInfo.getDepartment());
    }

    //得到所有身份，如本科生，研究生
    @Override
    public List<UserIdentity> getAllIdentity() {
        return VirtualThreadUtil.executor(()->userIdentityMapper.selectList(new QueryWrapper<UserIdentity>()
                .select("id","identity","allow_borrowing_count","allow_borrowing_duration")
                .eq("is_deleted",0)));
    }

    //批量添加用户
    @Override
    public Result addUser(List<AddUserVo> addUserVoList) {
        List<User> users = new ArrayList<>();
        List<UserDetailInfo> userDetailInfos = new ArrayList<>();
        //遍历集合，进行数据验证
        for (AddUserVo addUserVo : addUserVoList) {
            //用户是否存在
            if(VirtualThreadUtil.executor(()->userMapper.selectOne(new QueryWrapper<User>()
                    .eq("user_id",addUserVo.getUserId())
                    .eq("is_deleted",false))!=null)){
                    return Result.error().message(addUserVo.getUserId()+"已存在");
                }
            //用户身份是否存在
            UserIdentity userIdentity = VirtualThreadUtil.executor(()->userIdentityMapper.selectOne(new QueryWrapper<UserIdentity>()
                    .eq("is_deleted", 0).eq("identity", addUserVo.getIdentity())));
            if(userIdentity==null){
                return Result.error().message(addUserVo.getIdentity()+"身份不存在");
            }

            //添加用户信息进入数组
            User user = new User();
            String encodePassword = new BCryptPasswordEncoder(10).encode(addUserVo.getUserId().toString());
            BeanUtils.copyProperties(addUserVo,user);
            users.add(user.setPassword(encodePassword));

            //添加用户详细信息进入数组
            UserDetailInfo userDetailInfo = new UserDetailInfo();
            userDetailInfo.setUserId(addUserVo.getUserId())
                    .setSex(addUserVo.getSex().equals("男"))
                    .setCertificateStartDate(LocalDate.now())
                    .setCertificateEndDate(addUserVo.getCertificateEndDate())
                    .setBorrowingDuration(userIdentity.getAllowBorrowingDuration())
                    .setMaximumBorrowingCount(userIdentity.getAllowBorrowingCount());
            userDetailInfos.add(userDetailInfo);
        }
        if(users.isEmpty() ){
            return Result.error().message("插入数据为空");
        }
        //插入数据
        CompletableFuture<Boolean> res1 = VirtualThreadUtil
                .executorAsync(() -> userMapper.insertBatch(users) == addUserVoList.size());

        CompletableFuture<Boolean> res2 = VirtualThreadUtil
                .executorAsync(() -> userDetailMapper.insertBatch(userDetailInfos) == addUserVoList.size());

        return res1.thenCombine(res2,(a,b)->a&b).join()?Result.ok():Result.error();
    }

    //删除用户
    @Override
    public boolean deleteUser(Long userId) {
        return VirtualThreadUtil.executor(()->userMapper.update(new UpdateWrapper<User>().set("is_deleted",true).eq("user_id",userId))>0);
    }

    //延期用户凭证
    @Override
    public boolean extendUserVoucher(LocalDate date,Long userId) {
        CompletableFuture<Boolean> res1 = VirtualThreadUtil
                .executorAsync(() -> userDetailMapper.update(new UpdateWrapper<UserDetailInfo>().set("certificate_end_date", date).eq("user_id", userId)) > 0);

        CompletableFuture<Boolean> res2 = VirtualThreadUtil
                .executorAsync(() -> userMapper.update(new UpdateWrapper<User>().set("expire", false).eq("user_id", userId)) > 0);

        return res1.thenCombine(res2,(a,b)->a&b).join();
    }

    //查询用户是否绑定手机
    @Override
    public boolean queryUserHadPhone(Long userId) {
        return VirtualThreadUtil
                .executor(()->userMapper.selectOne(new QueryWrapper<User>().eq("user_id", userId)).getPhone()!=null);
    }

    //查询手机号是否被绑定
    @Override
    public boolean queryPhoneHasUser(Long phone) {
        return VirtualThreadUtil
                .executor(()->userMapper.selectOne(new QueryWrapper<User>().eq("phone", phone))!=null);
    }

    //更新用户信息
    @Override
    public boolean updateUserInfo(String phone, String password,Long userId) {
        return VirtualThreadUtil.executor(()->userMapper.update(new UpdateWrapper<User>()
                .set("password",new BCryptPasswordEncoder(10).encode(password))
                .set("phone",phone)
                .eq("user_id",userId))>0);
    }

    //得到用户消息
    @Override
    public Result getUserMessage(Long userId,int page,int limit) {
        try {
            //得到用户消息分页信息
            CompletableFuture<List<Message>> userMessages = VirtualThreadUtil.executorAsync(() -> messageMapper.selectMessageByUserId(userId, page, limit));
            CompletableFuture<Integer> total = VirtualThreadUtil.executorAsync(() -> messageMapper.selectMessageCountByUserId(userId));

            //移除提醒
            SpringContextUtil.getBean(WebsocketController.class).withdrawRemind(userId);

            return Result.ok().data("userMessages",userMessages.join()).data("total",total.join());
        } catch (IOException e) {
            throw new CustomizedException(20001,e.getMessage());
        }
    }

    //得到发送的信息
    @Override
    public List<Message> getSendMessage() {
        return VirtualThreadUtil
                .executor(()->messageMapper.selectList(new QueryWrapper<Message>().eq("is_deleted",0).eq("to_user_id",0)));
    }

    //删除发送的信息
    @Override
    public boolean deleteSendMessage(Long messageId) {
        return VirtualThreadUtil
                .executor(()->messageMapper.update(new UpdateWrapper<Message>().set("is_deleted",true).eq("id",messageId).eq("to_user_id",0))>0);
    }

    //发送消息
    @Override
    public Result sendMessage(Long userId, String title, String message) {
        User user = VirtualThreadUtil.executor(()->userMapper.selectOne(new QueryWrapper<User>().eq("user_id", userId)));
        if(user==null){
            return Result.error().message("用户不存在");
        }
        SpringContextUtil.getBean(WebsocketController.class).sendMessageToUser(new Message().setToUserId(0L).setMessage(message).setTitle(title).setFromUserId(userId));
        return Result.ok().message("发送成功");
    }
}
