package com.fjut.library_management_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fjut.library_management_system.util.GetSqlConnection;
import com.fjut.library_management_system.util.VirtualThreadUtil;
import com.fjut.library_management_system.entity.BookDetailInfo;
import com.fjut.library_management_system.entity.BookInfo;
import com.fjut.library_management_system.entity.BookStoreInfo;
import com.fjut.library_management_system.exception.CustomizedException;
import com.fjut.library_management_system.mapper.*;
import com.fjut.library_management_system.service.BookInfoService;
import com.fjut.library_management_system.util.Result;
import com.fjut.library_management_system.vo.AddBookVo;
import com.fjut.library_management_system.vo.BookChartVo;
import com.fjut.library_management_system.vo.BookInfoVo;
import com.fjut.library_management_system.vo.QueryBookVo;
import org.apache.ibatis.io.VFS;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Stream;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 叶良辰
 * @since 2024年03月31日
 */
@Service
public class BookInfoServiceImpl implements BookInfoService {

    private final BookInfoMapper bookInfoMapper;


    private final BookDetailInfoMapper bookDetailInfoMapper;


    private final BookStoreInfoMapper bookStoreInfoMapper;


    private final BookBorrowingInfoMapper bookBorrowingInfoMapper;


    private final UserMapper userMapper;

    @Autowired
    public BookInfoServiceImpl(BookInfoMapper bookInfoMapper, BookDetailInfoMapper bookDetailInfoMapper, BookStoreInfoMapper bookStoreInfoMapper, BookBorrowingInfoMapper bookBorrowingInfoMapper, UserMapper userMapper) {
        this.bookInfoMapper = bookInfoMapper;
        this.bookDetailInfoMapper = bookDetailInfoMapper;
        this.bookStoreInfoMapper = bookStoreInfoMapper;
        this.bookBorrowingInfoMapper = bookBorrowingInfoMapper;
        this.userMapper = userMapper;
    }

    //根据条件查询图书信息并分页
    @Override
    public Page<BookInfo> getBookInfo(QueryBookVo queryBookVo) {
        QueryWrapper<BookInfo> bookQuery = new QueryWrapper<>();
        //如果某个字段不为null，则将其添加进入查询条件
        bookQuery.select("book_id", "book_name", "author", "classification", "isbn", "publisher", "total", "remain")
                .like(queryBookVo.getBookName() != null && !queryBookVo.getBookName().isEmpty(), "book_name", queryBookVo.getBookName())
                .like(queryBookVo.getAuthor() != null && !queryBookVo.getAuthor().isEmpty(), "author", queryBookVo.getAuthor())
                .eq(queryBookVo.getClassification() != null && !queryBookVo.getClassification().isEmpty(), "classification", queryBookVo.getClassification())
                .eq(queryBookVo.getIsbn() != null && !queryBookVo.getIsbn().isEmpty(), "isbn", queryBookVo.getIsbn())
                .like(queryBookVo.getPublisher() != null && !queryBookVo.getPublisher().isEmpty(), "publisher", queryBookVo.getPublisher());

        //设置分页信息，返回分页结果对象
        return VirtualThreadUtil
                .executor(()->bookInfoMapper.selectPage(new Page<BookInfo>().setSize(queryBookVo.getLimit()).setCurrent(queryBookVo.getPage()), bookQuery));
    }

    //根据图书id查询图书详细信息
    @Override
    public BookInfoVo getBookDetailInfo(Integer bookId) {
        return VirtualThreadUtil
                .executor(()->bookInfoMapper.getBookDetailInfo(bookId, null));
    }

    //根据传入的BookInfoVo修改图书信息
    @Override
    public boolean updateBookInfo(BookInfoVo bookInfoVo) {
        //修改图书基本信息
        UpdateWrapper<BookInfo> bookInfo = new UpdateWrapper<BookInfo>()
                .set("book_name", bookInfoVo.getBookName())
                .set("isbn", bookInfoVo.getIsbn())
                .set("classification", bookInfoVo.getClassification())
                .set("author", bookInfoVo.getAuthor())
                .set("publisher", bookInfoVo.getPublisher())
                .eq("book_id", bookInfoVo.getBookId());
        CompletableFuture<Integer> res1 = VirtualThreadUtil
                .executorAsync(() -> bookInfoMapper.update(bookInfo));

        //修改图书详细信息
        UpdateWrapper<BookDetailInfo> bookDetailInfo = new UpdateWrapper<BookDetailInfo>()
                .set("parallel_title", bookInfoVo.getParallelTitle())
                .set("version_description", bookInfoVo.getVersionDescription())
                .set("book_size", bookInfoVo.getBookSize())
                .set("price", bookInfoVo.getPrice())
                .set("series", bookInfoVo.getSeries())
                .set("notes", bookInfoVo.getNotes())
                .set("abstract_info", bookInfoVo.getAbstractInfo())
                .set("subject", bookInfoVo.getSubject())
                .eq("book_id", bookInfoVo.getBookId());
        CompletableFuture<Integer> res2 = VirtualThreadUtil
                .executorAsync(() -> bookDetailInfoMapper.update(bookDetailInfo));

        return res1.join() > 0 && res2.join() > 0;
    }

    //通过bookId删除一本书籍
    @Override
    public boolean deleteBookInfo(Integer bookId) {
        //删除书籍信息，书籍详细信息，索书号信息
        CompletableFuture<Integer> res1 = VirtualThreadUtil
                .executorAsync(() -> bookInfoMapper.deleteById(bookId));
        CompletableFuture<Integer> res2 = VirtualThreadUtil
                .executorAsync(() -> bookDetailInfoMapper.delete(new QueryWrapper<BookDetailInfo>().eq("book_id", bookId)));
        CompletableFuture<Integer> res3 = VirtualThreadUtil
                .executorAsync(() -> bookStoreInfoMapper.delete(new QueryWrapper<BookStoreInfo>().eq("book_id", bookId)));
        return res1.join()>0&res2.join()>0&res3.join()>0;
    }

    //添加书籍详情
    @Override
    public Result addBookInfo(List<AddBookVo> addBookInfos) {
        //校验索书号是否与书籍数量匹配
        addBookInfos
                .parallelStream()
                .forEach(addBookVo -> {
                    String[] split = addBookVo.getCallNumber().split("&");
                    if(split.length != addBookVo.getCount()) {
                        throw  new CustomizedException(20001,addBookVo.getIsbn()+"：索书号数量与书籍数量不匹配");
                    }

                    for (String res : split) {
                        if(res.isEmpty()||res.length() > 30) {
                            throw  new CustomizedException(20001,addBookVo.getIsbn()+"：对应书籍索书号长度应在1到30之间");
                        }
                    }
                });


        List<BookDetailInfo> bookDetailInfos = Collections.synchronizedList(new ArrayList<>());
        List<BookStoreInfo> bookStoreInfos = Collections.synchronizedList(new ArrayList<>());



        for (AddBookVo addBookVo : addBookInfos) {
            String[] split = addBookVo.getCallNumber().split("&");
            //查询书籍是否已经存在
            BookInfo book = VirtualThreadUtil
                    .executor(()->bookInfoMapper.selectOne(new QueryWrapper<BookInfo>()
                             .eq("isbn", addBookVo.getIsbn())));

            //如果数据库中已经存在该isbn的书籍，则为仅需添加新书籍的数量和对应索书号
            if (book != null) {
                //更新已有书籍数量
                BookInfo finalBook = book;
                VirtualThreadUtil.executorAsync(()->{
                    bookInfoMapper.update(new UpdateWrapper<BookInfo>()
                            .eq("isbn", finalBook.getIsbn())
                            .set("total", finalBook.getTotal() + addBookVo.getCount())
                            .set("remain", finalBook.getRemain() + addBookVo.getCount()));
                });
            } else {
                //插入新书籍
                book = new BookInfo();
                BeanUtils.copyProperties(addBookVo, book);
                //插入后，mp会将书籍Id返回插入到Book中，书籍详细信息添加至bookDetailInfos，用于批量插入
                book.setTotal(addBookVo.getCount());
                BookInfo finalBook = book;
                CompletableFuture<Integer> res = VirtualThreadUtil
                        .executorAsync(() -> bookInfoMapper.insert(finalBook.setRemain(addBookVo.getCount())));

                BookDetailInfo bookDetailInfo = new BookDetailInfo();
                BeanUtils.copyProperties(addBookVo, bookDetailInfo);
                res.join();
                bookDetailInfos.add(bookDetailInfo.setBookId(book.getBookId()));
            }

            //将索书号添加到bookStoreInfos中，用于批量插入
            for (String callNumber : split) {
                bookStoreInfos.add(new BookStoreInfo()
                        .setBookId(book.getBookId()).setCallNumber(callNumber));
                Boolean res = VirtualThreadUtil.executor(() -> bookStoreInfoMapper.selectOne(new QueryWrapper<BookStoreInfo>()
                        .eq("call_number", callNumber)) != null);
                if (res) {
                    throw new CustomizedException(20001, "对于书籍" + addBookVo.getIsbn() + "，索书号" + callNumber + "已存在，无法添加");
                }
            }
        }

        CompletableFuture<Boolean> res1 = VirtualThreadUtil.executorAsync(() -> {
            if (!bookDetailInfos.isEmpty())
                return bookInfoMapper.insertBookDetailInfosBatch(bookDetailInfos) == bookDetailInfos.size();
            return true;
        });

        CompletableFuture<Boolean> res2 = VirtualThreadUtil.executorAsync(() -> {
            if (!bookStoreInfos.isEmpty())
                return bookStoreInfoMapper.insertBookStoreInfosBatch(bookStoreInfos) == bookStoreInfos.size();
            return true;
        });

        return res1.join()&res2.join()?Result.ok().message("添加成功"):Result.error().message("添加失败");
    }

    //通过isbn得到图书详细信息
    @Override
    public BookInfoVo getBookByIsbn(String isbn) {
        return VirtualThreadUtil
                .executor(()->bookInfoMapper.getBookDetailInfo(null, isbn));
    }

    //通过bookId得到索书号集合
    @Override
    public List<BookStoreInfo> getCallNumber(Integer bookId) {
        return VirtualThreadUtil
                .executor(()->bookStoreInfoMapper.selectList(new QueryWrapper<BookStoreInfo>().eq("book_id", bookId)));
    }

    //通过索书号删除对应索书号信息
    @Override
    public boolean deleteCallNumber(String callNumber, Integer bookId) {
        boolean res = true;
        //删除索书号信息，更新书籍总数和剩余数量
        CompletableFuture<Boolean> res1 = VirtualThreadUtil
                .executorAsync(() -> bookInfoMapper.update(new UpdateWrapper<BookInfo>().eq("book_id", bookId)
                    .setSql("total=total-1")
                    .setSql("remain=remain-1")) > 0);
        CompletableFuture<Boolean> res2 = VirtualThreadUtil
                .executorAsync(() -> bookStoreInfoMapper.delete(new QueryWrapper<BookStoreInfo>().eq("call_number", callNumber)) > 0);
        return res1.join()&res2.join();
    }

    //得到所有的书籍ID
    @Override
    public List<Integer> getAllBookId() {
        List<BookInfo> bookInfos = VirtualThreadUtil
                .executor(()->bookInfoMapper.selectList(new QueryWrapper<BookInfo>().select("book_id")));
        List<Integer> bookIds = new ArrayList<>();
        for (BookInfo bookInfo : bookInfos) {
            bookIds.add(bookInfo.getBookId());
        }
        return bookIds;
    }

    //得到所有的书籍基本信息和详细信息
    @Override
    public List<BookInfoVo> getAllBookDetailInfo(QueryBookVo queryBookVo) {
        return VirtualThreadUtil.executor(()->bookInfoMapper.getAllBookDetailInfo(queryBookVo));
    }

    //得到书籍、用户相关的数量信息，首页展示
    @Override
    public Result getCountInfo() {
        //开启倒计数器
        CountDownLatch countDownLatch = new CountDownLatch(4);

        //使用线程池，得到书籍总数、书库总数、借阅总数、用户总数
        CompletableFuture<Long> bookCount = VirtualThreadUtil
                .executorAsync(() -> bookInfoMapper.selectCount(new QueryWrapper<BookInfo>().eq("is_deleted", 0)),countDownLatch);

        CompletableFuture<Long> bookStoreCount = VirtualThreadUtil
                .executorAsync(() -> bookStoreInfoMapper.selectCount(new QueryWrapper<BookStoreInfo>().eq("is_deleted", 0)),countDownLatch);

        CompletableFuture<Long> bookBorrowingCount = VirtualThreadUtil
                .executorAsync(() -> bookBorrowingInfoMapper.selectCount(null), countDownLatch);

        CompletableFuture<Long> userCount = VirtualThreadUtil
                .executorAsync(() -> userMapper.selectCount(null), countDownLatch);

        try {
            //等待倒计数器归零，即四个线程全部完成任务
            countDownLatch.await();

            //成功返回结果
                return Result.ok().data("bookCount", bookCount.join())
                        .data("bookStoreCount", bookStoreCount.join())
                        .data("bookBorrowingCount", bookBorrowingCount.join())
                        .data("userCount", userCount.join());
        } catch (InterruptedException e) {
            throw new CustomizedException(20001,"中断异常");
        }
    }

    //得到书籍借阅排行榜,前20的书籍信息
    @Override
    public List<BookChartVo> getBookChartInfo() {
        return VirtualThreadUtil.executor(()->bookInfoMapper.getBookChartInfo());
    }
}


