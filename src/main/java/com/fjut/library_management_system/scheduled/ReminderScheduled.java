package com.fjut.library_management_system.scheduled;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fjut.library_management_system.util.SpringContextUtil;
import com.fjut.library_management_system.util.VirtualThreadUtil;
import com.fjut.library_management_system.controller.WebsocketController;
import com.fjut.library_management_system.entity.BookBorrowingInfo;
import com.fjut.library_management_system.entity.FineInfo;
import com.fjut.library_management_system.entity.Message;
import com.fjut.library_management_system.mapper.BookBorrowingInfoMapper;
import com.fjut.library_management_system.mapper.FineInfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

//定时任务
@Component
@Transactional
@Slf4j
public class ReminderScheduled {
    @Autowired
    private BookBorrowingInfoMapper bookBorrowingInfoMapper;

    @Autowired
    private FineInfoMapper fineInfoMapper;

    @Autowired
    private WebsocketController websocketController;

    // 每天0点提醒在接下来的7天内到期的借书记录
    @Scheduled(cron = "0 0 0 * * ?")
    public void reminderToReturnBook() {
        bookBorrowingInfoMapper.getDyingBorrowingBooks()
                //转换为流
                .stream()
                //异步发送消息
                .map((book)-> CompletableFuture.runAsync(
                        ()->websocketController.sendMessageToUser(new Message()
                                        .setTitle("还书提醒")
                                        .setToUserId(((BigInteger) book.get("user_id")).longValue())
                                        .setFromUserId(0L)
                                        .setMessage("您"+book.get("borrowing_date")+"借阅的《" + book.get("book_name") + "》还有"+book.get("duration")+"天到期，请尽快归还！"))
                                                , VirtualThreadUtil.virtualThreadPool
                        ))
                //等待所有异步任务完成
                .forEach(CompletableFuture::join);
    }

    // 每天0检查是否有超期未还的书籍，如果有则提醒用户并添加罚款信息
    @CacheEvict(value = "fineInfo", allEntries = true)
    @Scheduled(cron = "0 0 0 * * ?")
    public void RemindToPayFines(){
        WebsocketController websocketController = SpringContextUtil.getBean(WebsocketController.class);
        bookBorrowingInfoMapper.getOvertimeBorrowingBooks()
                .forEach(book -> {
                    CountDownLatch countDownLatch = new CountDownLatch(3);
                    long userId = ((BigInteger) book.get("user_id")).longValue();
                    long borrowingId = ((BigInteger) book.get("borrowing_id")).longValue();
                    BigDecimal price = BigDecimal.valueOf((Double) book.get("price"));

                    Thread.startVirtualThread(()->{
                        //消息提醒
                        websocketController.sendMessageToUser(new Message()
                                .setTitle("超期罚款提醒")
                                .setToUserId(userId)
                                .setFromUserId(0L)
                                .setMessage("您"+book.get("borrowing_date")+"借阅的《" + book.get("book_name") + "》已经逾期，逾期金额为"+
                                        price+"请尽快缴纳罚款，并归还书籍！"));
                        countDownLatch.countDown();
                    });

                    //添加罚款信息
                    Thread.startVirtualThread(()->{
                        fineInfoMapper.insert(new FineInfo()
                                .setBorrowingId(borrowingId)
                                .setFinePrice(price));
                        countDownLatch.countDown();
                    });

                    //更新该借阅记录以超时
                    Thread.startVirtualThread(()->{
                                bookBorrowingInfoMapper.update(new UpdateWrapper<BookBorrowingInfo>().eq("borrowing_id", borrowingId).set("overtime", 1));
                                countDownLatch.countDown();
                            }
                    );
                    try {
                        //等待所有异步任务完成
                        countDownLatch.await();
                    } catch (InterruptedException e) {
                        log.error("ReminderScheduled类RemindToPayFines方法线程中断异常");
                    }
                });
    }
}