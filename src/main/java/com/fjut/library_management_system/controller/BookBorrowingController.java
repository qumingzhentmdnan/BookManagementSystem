package com.fjut.library_management_system.controller;

import com.alibaba.excel.EasyExcel;
import com.fjut.library_management_system.util.VirtualThreadUtil;
import com.fjut.library_management_system.service.BookBorrowingInfoService;
import com.fjut.library_management_system.service.FineInfoService;
import com.fjut.library_management_system.util.RedisUtil;
import com.fjut.library_management_system.util.Result;
import com.fjut.library_management_system.util.excel.downLoadHandle;
import com.fjut.library_management_system.vo.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author 叶良辰
 * @since 2024年03月31日
 */
@RestController
@RequestMapping("/bookBorrowingInfo")
@Validated//开启数据校验
public class BookBorrowingController {
    private final BookBorrowingInfoService borrowingInfoService;;


    private final FineInfoService fineInfoService;

    @Autowired
    public BookBorrowingController(BookBorrowingInfoService borrowingInfoService, FineInfoService fineInfoService) {
        this.borrowingInfoService = borrowingInfoService;
        this.fineInfoService = fineInfoService;
    }

    /**
     * 查询借阅信息
     */
    // 通过注解的方式进行权限控制,拥有queryBorrowingInfo权限或者是当前用户访问自己的借阅信息采能够访问
    @PreAuthorize("#queryBorrowingVo.userId==T(com.fjut.library_management_system.util.SecurityUtil).getCurrentUsername() or hasAuthority('queryBorrowingInfo')")
    @GetMapping("/getBorrowingInfo")
    public Result getBorrowingInfo(QueryBorrowingVo queryBorrowingVo) {
        //得到借阅信息总数，用于分页
        CompletableFuture<Long> total = VirtualThreadUtil
                .executorAsync(() -> borrowingInfoService.getCount(queryBorrowingVo));

        //得到分页借阅信息
        CompletableFuture<List<BorrowingVo>> borrowingInfo =VirtualThreadUtil
                .executorAsync(() -> borrowingInfoService.getAllBorrowingInfo(queryBorrowingVo));

        return Result.ok().data("total", total.join()).data("borrowingInfo", borrowingInfo.join());
    }

    /**
     * 文件下载并且失败的时候返回json（默认失败了会返回一个有部分数据的Excel）
     *
     * @since 2.1.1
     */
    // 通过注解的方式进行权限控制,拥有exportBorrowingInfoExcel权限才能够访问
    @PreAuthorize("hasAuthority('exportBorrowingInfoExcel')")
    @PostMapping("/downloadBorrowingInfo")
    //exportVo是前端传过来的参数，包括了查询条件和导出的列
    public void downloadBorrowingInfo(HttpServletResponse response, @RequestBody ExportBorrowingExcelVo excelVo) throws IOException {
        try {
            //得到所有满足条件的所有借阅信息，这里不需要分页,所以page和limit设置为null
            List<BorrowingVo> allBorrowingInfo = borrowingInfoService.getAllBorrowingInfo(excelVo.getQueryBorrowingVo().setPage(null).setLimit(null));

            downLoadHandle.setExcelHead(response, "书籍借阅信息：" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH：mm：ss")) + "导出");

            //开始导出，这里需要设置不关闭流
            EasyExcel.write(response.getOutputStream(), BorrowingVo.class).autoCloseStream(Boolean.FALSE).sheet("书籍借阅信息")
                    .includeColumnFieldNames(excelVo.getExportList())
                    .doWrite(allBorrowingInfo);

        } catch (Exception e) {
            downLoadHandle.handleException(response, e);
        }
    }

    /**
     * 得到用户借阅书籍分类排行信息，用于构建首页图标
     */
    @Cacheable(value = "borrowingInfoSort", key = "'getBookBorrowingClassificationInfo'")
    @GetMapping("/getBookBorrowingClassificationInfo")
    public Result getBookBorrowingClassificationInfo() {
        return Result.ok().data("bookBorrowingClassificationInfo",borrowingInfoService.getBookBorrowingClassificationInfo());
    }

    /**
     * 得到当前用户借阅书籍分类，日期等信息，用于构建个人中心信息
     */
    @Cacheable(value = "userBorrowingInfo", key = "#userId+''+#year")
    @GetMapping("/getUserBorrowingChartInfo/{userId}/{year}")
    public Result getUserBorrowingInfo(@PathVariable("userId")
                                       @Range(min=1000000000, max=9999999999L, message = "请输入正确的10位身份凭证") Long userId,
                                       @PathVariable("year") @Min(value = 0, message = "年份不可以为负") Integer year) {
        Map<String, Object> userCenterInfo = borrowingInfoService.getUserCenterInfo(userId, year);

        return Result.ok()
                .data("borrowingMonthInfo", userCenterInfo.get("borrowingMonthInfo"))
                .data("borrowingClassificationInfo", userCenterInfo.get("borrowingClassificationInfo") );
    }

    /**
     * 归还书籍
     */
    @Cacheable(value = "userDetailInfo", key = "#userId")
    @PostMapping("/returnBook")
    public Result returnBook(@Range(min=1000000000, max=9999999999L, message = "请输入正确的10位身份凭证") Long userId
            , @NotNull String callNumber) {
        //删除缓存
        VirtualThreadUtil.executorAsync(()->new RedisUtil().removeCacheByPrefix("userBorrowingInfo::" + userId));

        //归还书籍
        return borrowingInfoService.returnBook(userId, callNumber);
    }

    /**
     * 借阅书籍
     */
    @Cacheable(value = "userDetailInfo", key = "#userId")
    @PostMapping("/borrowBook")
    public Result borrowBook(@Range(min=1000000000, max=9999999999L, message = "请输入正确的10位身份凭证") Long userId,
                             @NotNull String callNumber) {
        //删除缓存
        VirtualThreadUtil.executorAsync(()->new RedisUtil().removeCacheByPrefix("userBorrowingInfo::" + userId));

        //借阅书籍
        return  borrowingInfoService.borrowBook(userId, callNumber);
    }

    /**
     * 查询用户是否有未缴清的罚款记录
     */
    @GetMapping("/queryUserHasFies")
    public Result queryUserHasFies(@Range(min=1000000000, max=9999999999L, message = "请输入正确的10位身份凭证") Long userId) {
        boolean res = borrowingInfoService.queryUserHasFies(userId);
        return res ? Result.ok().message("该用户存在未缴清的罚款记录").data("res", true) : Result.ok().message("该用户不存在未缴清的罚款记录").data("res", false);
    }

    /**
     * 支付罚金
     * */
    //不做实现
    @PutMapping("/payFines")
    @PreAuthorize("hasAuthority('payFines')")
    public Result updateFineStatus(@Positive @NotNull Long payId){
        boolean res=borrowingInfoService.payFines(payId);
        if(res)
            new RedisUtil().removeCacheByPrefix("fineInfo");
        return res?Result.ok().message("缴费成功").data("res", true) : Result.ok().message("缴费失败").data("res", false);
    }

    /**
     * 查询罚款信息
     */
    // 通过注解的方式进行权限控制,拥有queryFineInfo权限或者是当前用户访问自己的罚款信息采能够访问
    @PreAuthorize("#queryFineVo.userId==T(com.fjut.library_management_system.util.SecurityUtil).getCurrentUsername() or hasAuthority('queryFineInfo')")
    @GetMapping("/getFineInfo")
    @Cacheable(value = "fineInfo", key = "#queryFineVo")
    public Result getFineInfo(@Validated QueryFineVo queryFineVo) {
        //得到分页罚款信息
        CompletableFuture<List<FineVo>> fineInfo =VirtualThreadUtil
                .executorAsync(() -> fineInfoService.getFineInfo(queryFineVo));

        //得到罚款信息总数，用于分页
        CompletableFuture<Long> total = VirtualThreadUtil
                .executorAsync(() -> fineInfoService.getFineCount(queryFineVo));

        return Result.ok().data("fineInfo", fineInfo.join()).data("total", total.join());
    }

    /**
     * 文件下载并且失败的时候返回json（默认失败了会返回一个有部分数据的Excel）
     *
     * @since 2.1.1
     */
    @PreAuthorize("hasAuthority('exportFineInfoExcel')")
    @PostMapping("/downloadFineInfo")
    public void downloadFineInfo(HttpServletResponse response, @RequestBody ExportFineExcelVo exportFineExcelVo) throws IOException {
        try {
            //得到所有满足条件的所有罚款信息，所以这里不需要分页
            List<FineVo> fineInfo = fineInfoService.getFineInfo(exportFineExcelVo.getQueryFineVo().setPage(null).setLimit(null));

            downLoadHandle.setExcelHead(response, "罚款信息：" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH：mm：ss")) + "导出");
            // 这里需要设置不关闭流
            EasyExcel.write(response.getOutputStream(), FineVo.class).autoCloseStream(Boolean.FALSE).sheet("罚款信息")
                    .includeColumnFieldNames(exportFineExcelVo.getExportList())
                    .doWrite(fineInfo);
        } catch (Exception e) {
            downLoadHandle.handleException(response, e);
        }
    }
}
