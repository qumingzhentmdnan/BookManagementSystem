package com.fjut.library_management_system.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fjut.library_management_system.entity.Message;
import com.fjut.library_management_system.entity.User;
import com.fjut.library_management_system.service.UserService;
import com.fjut.library_management_system.util.*;
import com.fjut.library_management_system.util.excel.AddUserExcelListen;
import com.fjut.library_management_system.util.excel.downLoadHandle;
import com.fjut.library_management_system.vo.AddUserVo;
import com.fjut.library_management_system.vo.ExportUserExcel;
import com.fjut.library_management_system.vo.QueryUserVo;
import com.fjut.library_management_system.vo.UserInfoVo;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author 叶良辰
 * @since 2024年03月31日
 */
@RestController
@RequestMapping("/user")
public class UserController {

    //从配置文件中获取的管理员账号
    @Value("${system.admin.accout}")
    private Long ADMIN_ID;
    private final UserService userService;

    private final Validator validator;

    @Resource
    private RedisTemplate<String,Object> stringRedisTemplate;

    @Autowired
    public UserController(UserService userService, Validator validator) {
        this.userService = userService;
        this.validator = validator;
    }

    /**
     * 方法作用：根据条件分页查询得到用户信息基本信息
     */
    @PreAuthorize("hasAuthority('queryUserInfo')")
    @GetMapping("/getUserInfo")
    @Cacheable(value = "userInfo", key = "#queryUserVo")
    public Result getUserInfo(@Validated QueryUserVo queryUserVo) {
        Page<User> userInfo = userService.getUserInfo(queryUserVo);
        return Result.ok().data("userInfo", userInfo);
    }

    /**
     * 方法作用：根据用户Id得到详细信息，用于管理员查看
     */
    @Cacheable(value = "userDetailInfo", key = "#userId")
    @PreAuthorize("hasAuthority('queryUserDetailInfo')")
    @GetMapping("/getUserDetailInfo")
    public Result getUserDetailInfo(@Range(min=1000000000, max=9999999999L, message = "请输入正确的10位身份凭证")
                                    Long userId) {
        UserInfoVo userDetailInfo = userService.getUserDetailInfo(userId);
        return Result.ok().data("userDetailInfo", userDetailInfo);
    }

    /**
     * 方法作用：根据用户Id封禁用户
     */
    @CacheEvict(value = "userInfo", allEntries = true)
    @PreAuthorize("hasAuthority('banUser')")
    @PutMapping("/banUser")
    public Result updateBanUser(@Range(min=1000000000, max=9999999999L, message = "请输入正确的10位身份凭证")
                          Long userId) {
        if (Objects.equals(userId, ADMIN_ID)) {
            return Result.error().message("无权封禁超级管理员账号");
        }
        VirtualThreadUtil.executorAsync(()-> stringRedisTemplate.delete("login:" + userId));
        System.out.println("login:" + userId);
        boolean res = userService.updateStatus(userId);
        return res ? Result.ok() : Result.error();
    }

    /**
     * 方法作用：根据用户Id解封用户
     */
    @CacheEvict(value = "userInfo", allEntries = true)
    @PreAuthorize("hasAuthority('unBanUser')")
    @PutMapping("/unBanUser")
    public Result updateUnBanUser(@Range(min=1000000000, max=9999999999L, message = "请输入正确的10位身份凭证")
                            Long userId) {
        boolean res = userService.updateStatus(userId);
        return res ? Result.ok() : Result.error();
    }

    /**
     * 文件下载并且失败的时候返回json（默认失败了会返回一个有部分数据的Excel）
     *
     * @since 2.1.1
     */
    @PreAuthorize("hasAuthority('exportUserInfoExcel')")
    @PostMapping("/downloadUserInfo")
    public void downloadUserInfo(HttpServletResponse response, @RequestBody ExportUserExcel exportUserExcel) throws IOException {
        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
        try {
            List<UserInfoVo> allUserDetailInfo = userService.getAllUserDetailInfo(exportUserExcel.getQueryUserVo().setPage(null).setLimit(null));
            downLoadHandle.setExcelHead(response, "书籍信息：" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH：mm：ss")) + "导出");
            // 这里需要设置不关闭流
            EasyExcel.write(response.getOutputStream(), UserInfoVo.class).autoCloseStream(Boolean.FALSE).sheet("书籍信息")
                    .includeColumnFieldNames(exportUserExcel.getExportList())
                    .doWrite(allUserDetailInfo);
        } catch (Exception e) {
            downLoadHandle.handleException(response, e);
        }
    }

    /**
     * 得到用户借阅书籍排行信息，用于构建首页图标
     */
    @Cacheable(value = "const", key = "'getUserBorrowingSortInfo'")
    @GetMapping("/getUserBorrowingSortInfo")
    public Result getUserBorrowingSortInfo() {
        return Result.ok().data("userSortBorrowingInfo", userService.getUserBorrowingSortInfo());
    }

    /**
     * 用于得到个人中心的信息
     */
    @Cacheable(value = "getPersonalCenterInfo", key = "T(com.fjut.library_management_system.util.SecurityUtil).getCurrentUsername()")
    @GetMapping("/getPersonalCenterInfo")
    public Result getPersonalCenterInfo() {
        return Result.ok().data("userInfo", userService.getUserDetailInfo(SecurityUtil.getCurrentUsername()));
    }

    /**
     * 用于得到全局的个人信息，权限，头像等等
     */
    @Cacheable(value = "globalUserInfo", key = "T(com.fjut.library_management_system.util.SecurityUtil).getCurrentUsername()")
    @GetMapping("/getGlobalUserInfo")
    public Result getGlobalUserInfo() {
        return userService.getGlobalUserInfo(SecurityUtil.getCurrentUsername());
    }

    /**
     * 判断用户是否登录
     */
    @GetMapping("/isLogin")
    public Result isLogin() {
        //判断用户是否登录，如果登录了，再判断是否绑定了手机号
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //获取登录状态
        Boolean isLogin = Optional.ofNullable(authentication)
                //存在认证信息，且不是匿名用户，且已经认证
                .map(auth -> !authentication.getPrincipal().equals("anonymousUser")
                        && authentication.isAuthenticated())
                //判断是否绑定了手机号，未绑定手机号不允许登录
                .map(res->
                        res&userService.queryUserHadPhone(SecurityUtil.getCurrentUsername()))
                .orElse(false);

        return Result.ok().data("isLogin", isLogin);
    }

    /**
     * 得到七牛云的token，用于用户文件上传
     */
    @GetMapping("/getQiniuToken")
    public Result getQiniuToken() {
        //判断用户是否频繁更换头像,24小时内最多只能更换3次
        Long currentUsername = SecurityUtil.getCurrentUsername();
        Object o = VirtualThreadUtil.executor(()-> stringRedisTemplate.opsForValue().get("getTokenCount:" + currentUsername));
        if (o == null) {
            VirtualThreadUtil.executorAsync(()-> stringRedisTemplate.opsForValue().set("getTokenCount:" + currentUsername, 1, 24, TimeUnit.HOURS));
        } else {
            if ((Integer) o > 3)
                return Result.error().message("头像更换过于频繁");
            VirtualThreadUtil.executorAsync(()-> stringRedisTemplate.opsForValue().increment("getTokenCount:" + currentUsername));
        }
        String accessKey = "W2TYnJQ46UdNYlmy2dRgCHAq6Q_B4vliCwtwWsAD";
        String secretKey = "ltfgxHqCkmRk-NYXfkE7DjJy7gdgu3mopqPAeRfm";
        String bucket = "library-management-system";
        Auth auth = Auth.create(accessKey, secretKey);
        //上传策略：限制上传文件大小为0-2M，Token有效期为3600s
        CompletableFuture<String> upToken = VirtualThreadUtil
                .executorAsync(() -> auth.uploadToken(bucket, null, 3600L, new StringMap().put("fsizeMin", 1).put("fsizeLimit", 1024 * 1024 * 2)));
        return Result.ok().data("qiniuToken", upToken.join());
    }

    /**
     * 修改用户头像
     */
    @PutMapping("/updateAvatar")
    @Caching(evict = {
            @CacheEvict(value = "globalUserInfo", key = "#map.get('username')"),
            @CacheEvict(value = "userDetailInfo", key = "#map.get('username')"),
            @CacheEvict(value = "userInfo", allEntries = true),
            @CacheEvict(value = "getPersonalCenterInfo", key = "#map.get('username')")
    })
    public Result updateAvatar(@RequestBody HashMap<String, String> map) {
        boolean res = userService.updateAvatar(map.get("avatar"), SecurityUtil.getCurrentUsername());
        return res ? Result.ok() : Result.error();
    }

    /**
     * 得到所有的身份信息：id，身份（本科生、博士等等），允许借阅数量，允许借阅时长
     */
    @Cacheable(value = "const", key = "'getAllIdentity'")
    @GetMapping("/getAllIdentity")
    public Result getAllIdentity() {
        return Result.ok().data("identity", userService.getAllIdentity());
    }


    /**
     * 添加一个用户信息
     */
    @PostMapping("/addUser")
    @CacheEvict(value = "userInfo", allEntries = true)
    public Result addUser(@Validated @RequestBody AddUserVo addUserVo) {
        return userService.addUser(List.of(addUserVo));
    }

    /**
     * 通过Excel批量添加用户
     */
    @CacheEvict(value = "userInfo", allEntries = true)
    @PostMapping("/addUserByExcel")
    public Result uploadUserInfo(MultipartFile file) throws IOException {
        //判断文件类型,只允许上传Excel文件
        String contentType = file.getContentType();
        if (!"application/vnd.ms-excel".equals(contentType) &&
                !"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(contentType)) {
            return Result.error().message("文件类型不正确，必须为Excel文件");
        }
        EasyExcel.read(file.getInputStream(), AddUserVo.class, new AddUserExcelListen(userService, validator)).sheet().doRead();
        return Result.ok();
    }

    /**
     * 删除用户
     */
    @CacheEvict(value = "userInfo", allEntries = true)
    @DeleteMapping("/deleteUser")
    public Result deleteUser(@Range(min=1000000000, max=9999999999L, message = "请输入正确的10位身份凭证")
                             Long userId) {
        if (Objects.equals(userId, ADMIN_ID)) {
            return Result.error().message("无权删除超级管理员账号");
        }
        VirtualThreadUtil.executorAsync(()-> stringRedisTemplate.delete("login:" + userId));
        boolean res = userService.deleteUser(userId);
        return res ? Result.ok() : Result.error();
    }

    /**
     * 延期用户的凭证
     */
    @CacheEvict(value = "userInfo", allEntries = true)
    @PutMapping("/extendUserVoucher")
    public Result updateUserVoucher(String date,
                                    @Range(min=1000000000, max=9999999999L, message = "请输入正确的10位身份凭证")Long userId) {
        LocalDate localDate = LocalDate.parse(date);
        if (localDate.isBefore(LocalDate.now())) {
            return Result.error().message("延长日期不可以小于当前日期");
        }
        boolean res = userService.extendUserVoucher(localDate, userId);
        return res ? Result.ok().message("延长成功") : Result.error().message("延长失败");
    }

    /**
     * 得到验证码
     */
    @GetMapping("/getPhoneCode")
    public Result getPhoneCode(@Pattern(regexp = "(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\\d{8}",
            message = "请输入正确格式的手机号") String phone) {
        return PhoneCodeUtil.getCode(SecurityUtil.getCurrentUsername(), phone, stringRedisTemplate);
    }

    /**
     * 首次登录，修改个人信息
     */
    @CacheEvict(value = "userInfo", allEntries = true)
    @PostMapping("/updateUserInfo")
    public Result updateUserInfo(@RequestBody HashMap<String, String> map) throws IOException {
        String password = map.get("password");
        String phone = map.get("phone");

        //验证验证码
        Long currentUserId = SecurityUtil.getCurrentUsername();
        Object o = stringRedisTemplate.opsForValue().get(currentUserId + ":" + phone + ":code");

        if (o == null) {
            return Result.error().message("请先获取验证码");
        }
        if (!o.equals(map.get("code"))) {
            return Result.error().message("验证码错误");
        }

        //验证手机号是否已经存在
        if (userService.queryPhoneHasUser(Long.valueOf(phone))) {
            return Result.error().message("手机号已存在,无法绑定");
        }

        //验证密码是否满足格式
        if (!password.matches("^[a-zA-Z0-9]{6,16}$")) {
            return Result.error().message("密码不符合要求，请输入6-16位数字或字母");
        }
        userService.updateUserInfo(phone, password, currentUserId);

        //发送消息
        WebsocketController message = SpringContextUtil.getBean(WebsocketController.class);
        message.sendMessageToUser(new Message().setToUserId(currentUserId).setMessage("您的账号已经绑定手机号").setTitle("绑定成功").setFromUserId(0L));
        return Result.ok();
    }
}
