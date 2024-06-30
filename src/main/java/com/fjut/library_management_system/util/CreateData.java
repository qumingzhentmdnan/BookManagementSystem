package com.fjut.library_management_system.util;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fjut.library_management_system.entity.BookBorrowingInfo;
import com.fjut.library_management_system.entity.BookStoreInfo;
import com.fjut.library_management_system.entity.FineInfo;
import com.fjut.library_management_system.entity.UserDetailInfo;
import com.fjut.library_management_system.mapper.BookDetailInfoMapper;
import com.fjut.library_management_system.mapper.BookStoreInfoMapper;
import com.fjut.library_management_system.mapper.UserDetailMapper;
import com.fjut.library_management_system.service.BookBorrowingInfoService;
import com.fjut.library_management_system.service.BookInfoService;
import com.fjut.library_management_system.service.FineInfoService;
import com.fjut.library_management_system.service.UserService;
import com.fjut.library_management_system.vo.QueryFineVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

//随机生成数据
public class CreateData {


//    @Autowired
//    private BookBorrowingInfoService bookLengdingInfoService;
//    @Autowired
//    private FineInfoService fineInfoService;
//    @Autowired
//    private UserDetailMapper.xml userDetailMapper;
//    @Autowired
//    private UserService userService;
//    @Autowired
//    private BookInfoService bookInfoService;
//    @GetMapping("/createData")
//    public Result createData() {
//        List<Long> allUserId = userService.getAllUserId();
//        List<Integer> allBookId =bookInfoService.getAllBookId();
//
//        LocalDate now = LocalDate.now().minusYears(10);
//        ArrayList<BookBorrowingInfo> leadingList = new ArrayList<>();
//        Random random = new Random();
//        int size=0;
//        BookDetailInfoMapper bookDetailMapper = bookInfoService.getBookDetailMapper();
//        BookStoreInfoMapper bookStoreMapper = bookInfoService.getBookStoreMapper();
//        while(!now.equals(LocalDate.now())) {
//            LocalDate finalNow = now;
//            leadingList.clear();
//            //每天随机生成20条借书记录
//            int res=random.nextInt(15,25);
//            for(int i = 0; i < res; i++) {
//                int userId = random.nextInt(1117);
//                int bookId = random.nextInt(13637);
//                BookStoreInfo bookStoreInfo = bookStoreMapper.selectOne(new QueryWrapper<BookStoreInfo>().eq("book_id", allBookId.get(bookId)).eq("is_lengding", false).last("limit 1"));
//                if(bookStoreInfo!=null){
//                    bookStoreMapper.update(new UpdateWrapper<BookStoreInfo>().eq("id", bookStoreInfo.getId())
//                            .setSql("is_lengding=1"));
//                    BookBorrowingInfo bookBorrowingInfo = new BookBorrowingInfo().setBookId(allBookId.get(bookId))
//                            .setUserId(allUserId.get(userId))
//                            .setBorrowingDate(now)
//                            .setStoreId(bookStoreInfo.getId());
//                    leadingList.add(bookBorrowingInfo);
//                    userDetailMapper.update(new UpdateWrapper<UserDetailInfo>().eq("user_id", allUserId.get(userId))
//                            .setSql("total_borrowing_count=total_borrowing_count+1")
//                            .setSql("already_borrowing_count=already_borrowing_count+1"));
//
//                }
//            }
//            bookLengdingInfoService.saveBatch(leadingList);
//
//            //每天随机生成17条还书记录
//            List<BookBorrowingInfo> isReturn = bookLengdingInfoService.list(new QueryWrapper<BookBorrowingInfo>()
//                    .eq("returned", 0)
//                    .orderByAsc("borrowing_date")
//                    .last("limit 200"));
//            Collections.shuffle(isReturn);
//            List<BookBorrowingInfo> bookBorrowingInfos;
//            if(isReturn.size()<150)
//                bookBorrowingInfos = isReturn.subList(0, Math.min(random.nextInt(17,22), isReturn.size()));
//            else
//                bookBorrowingInfos = isReturn.subList(0, Math.min( isReturn.size(),random.nextInt(20,500)));
//            bookBorrowingInfos.forEach((bookLengdingInfo) -> {
//                bookLengdingInfo.setReturnDate(finalNow);
//                bookLengdingInfo.setReturned(true);
//                userDetailMapper.update(new UpdateWrapper<UserDetailInfo>().eq("user_id", bookLengdingInfo.getUserId())
//                        .setSql("already_borrowing_count=already_borrowing_count-1"));
//                bookStoreMapper.update(new UpdateWrapper<BookStoreInfo>().eq("id", bookLengdingInfo.getStoreId())
//                        .setSql("is_lengding=0"));
//            });
//            bookLengdingInfoService.updateBatchById(bookBorrowingInfos);
//
//            List<FineInfo> fineInfos = new ArrayList<>();
//
//            //查看是否有逾期的书籍
//            List<BookBorrowingInfo> book = bookLengdingInfoService.list(new QueryWrapper<BookBorrowingInfo>().eq("returned", 0).eq("overtime",0).last("limit " + 100));
//            book.stream().filter((bookLengdingInfo) -> bookLengdingInfo.getBorrowingDate().isBefore(finalNow.minusDays(30))).forEach((bookLengdingInfo) -> {
//                fineInfos.add(new FineInfo().setBorrowingId(bookLengdingInfo.getBorrowingId())
//                        .setFinePrice(new BigDecimal(random.nextInt(15,30))));
//                bookLengdingInfoService.update(new UpdateWrapper<BookBorrowingInfo>().eq("borrowing_id", bookLengdingInfo.getBorrowingId())
//                        .setSql("overtime=1"));
//                userDetailMapper.update(new UpdateWrapper<UserDetailInfo>().eq("user_id", bookLengdingInfo.getUserId())
//                        .setSql("number_of_violations=number_of_violations+1"));
//            });
//            fineInfoService.saveBatch(fineInfos);
//            size+=fineInfos.size();
//            System.out.println(now+"size = " + size);
//            Long fineCount = fineInfoService.getFineCount(new QueryFineVo().setPayed(false));
//            int count=2;
//            //每天随机生成两条还款记录
//            List<FineInfo> isPaying = fineInfoService.list(new QueryWrapper<FineInfo>().eq("payed", 0).last("ORDER BY RAND() limit " + count));
//            isPaying.forEach((fineInfo) -> {
//                fineInfo.setPayed(true);
//                fineInfo.setPayingDate(finalNow);
//            });
//            fineInfoService.updateBatchById(isPaying);
//            now = now.plusDays(1);
//        }
//        return Result.ok();
//    }
}