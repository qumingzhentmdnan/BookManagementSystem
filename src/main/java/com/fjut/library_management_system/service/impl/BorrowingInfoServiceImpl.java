package com.fjut.library_management_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fjut.library_management_system.util.SpringContextUtil;
import com.fjut.library_management_system.controller.WebsocketController;
import com.fjut.library_management_system.entity.*;
import com.fjut.library_management_system.mapper.*;
import com.fjut.library_management_system.service.BookBorrowingInfoService;
import com.fjut.library_management_system.util.Result;
import com.fjut.library_management_system.util.VirtualThreadUtil;
import com.fjut.library_management_system.vo.BorrowingClassificationChartVo;
import com.fjut.library_management_system.vo.BorrowingMonthChartVo;
import com.fjut.library_management_system.vo.BorrowingVo;
import com.fjut.library_management_system.vo.QueryBorrowingVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
public class BorrowingInfoServiceImpl extends ServiceImpl<BookBorrowingInfoMapper, BookBorrowingInfo> implements BookBorrowingInfoService {


    private final BookBorrowingInfoMapper bookBorrowingInfoMapper;


    private final BookStoreInfoMapper bookStoreInfoMapper;


    private final BookInfoMapper bookInfoMapper;

    @Autowired
    public BorrowingInfoServiceImpl(BookBorrowingInfoMapper bookBorrowingInfoMapper, BookStoreInfoMapper bookStoreInfoMapper, BookInfoMapper bookInfoMapper) {
        this.bookBorrowingInfoMapper = bookBorrowingInfoMapper;
        this.bookStoreInfoMapper = bookStoreInfoMapper;
        this.bookInfoMapper = bookInfoMapper;
    }

    //根据条件查询借阅信息
    @Override
    public List<BorrowingVo> getAllBorrowingInfo(QueryBorrowingVo queryBorrowingVo) {
        //如果没有分页信息，则查询所有
        if(queryBorrowingVo.getPage()==null||queryBorrowingVo.getLimit()==null){
            return VirtualThreadUtil.executor(()->bookBorrowingInfoMapper.getAllBorrowingInfo(queryBorrowingVo, null));
        }else{
            //如果有分页信息，则根据分页信息查询
            return VirtualThreadUtil.executor(()->bookBorrowingInfoMapper.getAllBorrowingInfo(queryBorrowingVo, (long) (queryBorrowingVo.getPage() - 1) *queryBorrowingVo.getLimit()));
        }

    }
    //得到借阅数量
    @Override
    public Long getCount(QueryBorrowingVo queryBorrowingVo) {
        return VirtualThreadUtil.executor(()->bookBorrowingInfoMapper.getBorrowingCount(queryBorrowingVo));
    }

    //得到总体借阅书籍的分类信息
    @Override
    public List<BorrowingClassificationChartVo> getBookBorrowingClassificationInfo() {
        return VirtualThreadUtil.executor(()->bookBorrowingInfoMapper.getBookBorrowingClassificationInfo());
    }


    //得到某个用户的借阅月份信息和借阅分类信息
    @Override
    public Map<String,Object> getUserCenterInfo(Long userId, Integer year) {
        CompletableFuture<List<BorrowingClassificationChartVo>> borrowingClassificationInfo = VirtualThreadUtil
                .executorAsync(() -> bookBorrowingInfoMapper.getBorrowingClassificationInfoByUserId(userId, year));
        CompletableFuture<List<BorrowingMonthChartVo>> borrowingMonthInfo = VirtualThreadUtil

                .executorAsync(() -> bookBorrowingInfoMapper.getBorrowingMonthInfoByUserId(userId, year));
        return Map.of("borrowingMonthInfo",borrowingMonthInfo.join(),"borrowingClassificationInfo",borrowingClassificationInfo.join());
    }

    //还书
    @Override
    public Result returnBook(Long userId, String callNumber) {
        //查询是否存在借阅信息
        HashMap<String, Object> returnBookInfo = VirtualThreadUtil.executor(()->bookBorrowingInfoMapper.getReturnBookInfo(userId, callNumber));
        if(returnBookInfo==null){
            return Result.error().message("未查询到的借阅信息");
        }
        Integer borrowingId = Integer.valueOf(returnBookInfo.get("borrowing_id").toString());

        //设置当前借阅状态为已归还
        VirtualThreadUtil.executorAsync(()->{
            bookBorrowingInfoMapper.update(new UpdateWrapper<BookBorrowingInfo>()
                    .eq("borrowing_id", borrowingId)
                    .set("returned",true)
                    .set("return_date", LocalDate.now()));
        });

        //设置书籍库存状态为未借出
        VirtualThreadUtil.executorAsync(()->{
            bookStoreInfoMapper.update(new UpdateWrapper<BookStoreInfo>()
                    .set("is_borrowing",false)
                    .set("borrowing_date",null)
                    .set("return_date",null)
                    .eq("call_number",callNumber));
        });

        //修改书籍剩余数量
        VirtualThreadUtil.executorAsync(()->{
            bookInfoMapper.update(new UpdateWrapper<BookInfo>()
                    .setSql("remain=remain+1")
                    .eq("book_id", returnBookInfo.get("book_id")));
        });

        //向用户发送还书提醒
        WebsocketController websocketController = SpringContextUtil.getBean(WebsocketController.class);
        websocketController.sendMessageToUser(new Message()
                .setToUserId(userId)
                .setFromUserId(0L)
                .setTitle("书籍归还成功")
                .setMessage("您于"+returnBookInfo.get("borrowing_date")+"借阅的书籍《"+returnBookInfo.get("book_name")+"》已归还成功"));
        return Result.ok().message("归还成功");
    }

    //借书
    @Override
    public Result borrowBook(Long userId, String callNumber) {
        //查询用户是否有未缴纳的罚金
        if(queryUserHasFies(userId)){
            return Result.error().message("您有未缴纳的罚金，请先缴纳罚金,再借阅书籍");
        }

        //查询书籍是否存在
        BookStoreInfo bookStoreInfo = VirtualThreadUtil
                .executor(()->bookStoreInfoMapper.selectOne(new QueryWrapper<BookStoreInfo>().eq("call_number", callNumber)));
        if(bookStoreInfo==null){
            return Result.error().message("未入库书籍，请联系管理员");
        }

        //修改书籍剩余数量
        CompletableFuture<BookInfo> bookInfo = VirtualThreadUtil.executorAsync(() ->
                bookInfoMapper.selectOne(new QueryWrapper<BookInfo>().select("book_name").eq("book_id", bookStoreInfo.getBookId()))
        );

        VirtualThreadUtil.executorAsync(()->{
            bookInfoMapper.update(new UpdateWrapper<BookInfo>()
                    .setSql("remain=remain-1")
                    .eq("book_id", bookStoreInfo.getBookId()));
        });

        //设置书籍库存状态为已借出
        UserDetailInfo userDetailInfo = VirtualThreadUtil
                .executor(()->SpringContextUtil.getBean(UserDetailMapper.class)
                        .selectOne(new QueryWrapper<UserDetailInfo>().select("borrowing_duration").eq("user_id", userId)));

        VirtualThreadUtil.executorAsync(()->bookStoreInfoMapper.update(new UpdateWrapper<BookStoreInfo>()
                .set("is_borrowing",true)
                .set("borrowing_date",LocalDateTime.now())
                .set("return_date",LocalDateTime.now().plusDays(userDetailInfo.getBorrowingDuration()))
                .eq("call_number",callNumber)));


        //添加一条借阅信息
        bookBorrowingInfoMapper.insert(new BookBorrowingInfo()
                .setBookId(bookStoreInfo.getBookId())
                .setUserId(userId)
                .setBorrowingDate(LocalDate.now())
                .setStoreId(bookStoreInfo.getId()));

        //向用户发送信息提醒
        WebsocketController websocketController = SpringContextUtil.getBean(WebsocketController.class);
        websocketController.sendMessageToUser(new Message()
                .setToUserId(userId)
                .setFromUserId(0L)
                .setTitle("书籍借阅成功")
                .setMessage("您于"+LocalDateTime.now()+"借阅的书籍《"+bookInfo.join().getBookName()
                        +"》，最长借阅时间最长为"+userDetailInfo.getBorrowingDuration()+"天。请在"
                        +LocalDateTime.now().plusDays(userDetailInfo.getBorrowingDuration())+"前归还。避免违约"));

        return Result.ok().message("借阅成功");
    }

    //查询用户是否有未缴纳的罚金
    @Override
    public boolean queryUserHasFies(Long userId) {
        FineInfoMapper fineInfoMapper = SpringContextUtil.getBean(FineInfoMapper.class);
        return VirtualThreadUtil.executor(()->fineInfoMapper.queryUserHasFies(userId)!=0);
    }

    @Override
    public boolean payFines(Long payId) {

        FineInfoMapper fineInfoMapper = SpringContextUtil.getBean(FineInfoMapper.class);

        return VirtualThreadUtil.executor(
                ()-> fineInfoMapper.update(new UpdateWrapper<FineInfo>()
                        .set("payed",true)
                        .eq("id",payId))>0
        );
    }
}
