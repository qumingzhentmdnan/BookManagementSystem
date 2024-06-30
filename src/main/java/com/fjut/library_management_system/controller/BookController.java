package com.fjut.library_management_system.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fjut.library_management_system.entity.BookInfo;
import com.fjut.library_management_system.service.BookInfoService;
import com.fjut.library_management_system.util.Result;
import com.fjut.library_management_system.util.excel.AddBookExcelListen;
import com.fjut.library_management_system.util.excel.downLoadHandle;
import com.fjut.library_management_system.vo.AddBookVo;
import com.fjut.library_management_system.vo.BookInfoVo;
import com.fjut.library_management_system.vo.ExportBookExcelVo;
import com.fjut.library_management_system.vo.QueryBookVo;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author 叶良辰
 * @since 2024年03月31日
 */
@RestController
@RequestMapping("/book")
@Validated//开启数据校验
public class BookController {
    private final BookInfoService bookInfoService;


    private final Validator validator;

    @Autowired
    public BookController(BookInfoService bookInfoService, Validator validator) {
        this.bookInfoService = bookInfoService;
        this.validator = validator;
    }

    /**
     * 方法作用：根据条件查询图书信息并分页
     * @Validated 开启SpringBootValidation的数据校验
     * */
    @Cacheable(value = "bookInfo", key = "#queryBookVo")
    @GetMapping("/getBookInfo")
    public Result getBookInfo(@Validated  QueryBookVo queryBookVo) {
        //根据传入的QueryBookVo查询图书信息，并分页
        Page<BookInfo> bookInfo = bookInfoService.getBookInfo(queryBookVo);
        return Result.ok().data("bookInfo", bookInfo);
    }

    /**
     * 方法作用：根据图书id得到详细的图书信息
     * */
    @Cacheable(value = "bookDetailInfo", key = "#bookId")
    @GetMapping("/getBookDetailInfo/{bookId}")
    public Result getBookDetailInfo(@PathVariable("bookId") @Min(value = 0,message = "图书Id不可以为负")Integer bookId) {
        return Result.ok().data("bookDetailInfo", bookInfoService.getBookDetailInfo(bookId));
    }

    /**
     * 方法作用：根据传入QueryBookVo修改图书信息
     * */
    @Caching(evict = {
            @CacheEvict(value = "bookInfo", allEntries = true),
            @CacheEvict(value = "bookDetailInfo", key = "#bookInfoVo.bookId")
    })
    @PreAuthorize("hasAuthority('updateBook')")
    @PutMapping("/updateBookInfo")
    public Result updateBookInfo(@Validated @RequestBody BookInfoVo bookInfoVo) {
        boolean res = bookInfoService.updateBookInfo(bookInfoVo);
        return res?Result.ok().message("图书信息修改成功"):Result.error().message("图书信息修改失败");
    }

    /**
     *方法作用：根据传入bookId删除图书信息
     * */
    @Caching(evict = {
            @CacheEvict(value = "bookInfo", allEntries = true),
            @CacheEvict(value = "bookDetailInfo", key = "#bookId")
    })
    @PreAuthorize("hasAuthority('deleteBook')")
    @DeleteMapping("/deleteBookInfo/{bookId}")
    public Result deleteBookInfo(@PathVariable("bookId") @Min(value = 0,message = "图书Id不可以为负") Integer bookId) {
        boolean res = bookInfoService.deleteBookInfo(bookId);
        return res?Result.ok().message("删除成功"):Result.error().message("删除失败");
    }

    /**
     * 方法作用：根据传入的callNumber删除索书号信息
     * */
    @Caching(evict = {
            @CacheEvict(value = "getCallNumber", key = "#bookId"),
            @CacheEvict(value = "bookInfo", allEntries = true)
    })
    @PreAuthorize("hasAuthority('deleteBookStore')")
    @DeleteMapping("/deleteCallNumber")
    public Result deleteCallNumber(@NotNull(message = "索书号不能为空")
                                   @Pattern(regexp = "^.{0,30}$", message = "索书号长度应该在0-30字符")String callNumber,
                                   @NotNull(message = "索书号不能为空")
                                   @Min(value=0, message = "图书id不能为负数") Integer bookId) {
        boolean res = bookInfoService.deleteCallNumber(callNumber, bookId);
        return res?Result.ok().message("删除成功"):Result.error().message("删除失败");
    }

    /**
     * 方法作用：根据isbn码查询图书详情
     * */
    @Cacheable(value = "getBookByIsbn", key = "#isbn")
    @GetMapping("/getBookByIsbn")
    public Result getBookCount(@Pattern(regexp = "^.{1,13}$", message = "isbn长度不符合规范，请输入10位或13位isbn码，无需分隔符")
                                   @NotNull(message="isbn不能为空") String isbn) {
        return Result.ok().data("bookInfo", bookInfoService.getBookByIsbn(isbn));
    }

    /**
     * 方法作用：根据传入AddBookVo添加图书信息
     * */
    @Caching(evict = {
            @CacheEvict(value = "bookInfo", allEntries = true),
            @CacheEvict(value = "getCallNumber", key = "#bookInfo.getBookId()",condition = "#bookInfo.getBookId()!=null"),
    })
    @PreAuthorize("hasAuthority('addBook')")
    @PostMapping("/addBookInfo")
    public Result addBookInfo(@Validated @RequestBody AddBookVo bookInfo) {
        return bookInfoService.addBookInfo(List.of(bookInfo));
    }

    /**
     * 方法作用：根据传入bookId查询索书号
     * */
    @Cacheable(value = "getCallNumber", key = "#bookId")
    @GetMapping("/getCallNumber/{bookId}")
    public Result getCallNumber(@PathVariable("bookId") @Min(value = 0,message = "图书Id不可以为负") Integer bookId) {
        return Result.ok().data("callNumber", bookInfoService.getCallNumber(bookId));
    }

    /**
     * 文件下载并且失败的时候返回json（默认失败了会返回一个有部分数据的Excel）
     *
     * @since 2.1.1
     */
    @PreAuthorize("hasAuthority('exportUserInfoExcel')")
    @PostMapping("/downloadBookInfo")
    public void downloadBookInfo(HttpServletResponse response, @RequestBody ExportBookExcelVo exportBookExcelVo) throws IOException {
        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
        try {
            List<BookInfoVo> allBookDetailInfo = bookInfoService.getAllBookDetailInfo(exportBookExcelVo.getQueryBookVo());
            downLoadHandle.setExcelHead(response, "书籍信息："+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH：mm：ss"))+"导出");
            // 这里需要设置不关闭流
            EasyExcel.write(response.getOutputStream(), BookInfoVo.class).autoCloseStream(Boolean.FALSE).sheet("书籍信息")
                    .includeColumnFieldNames(exportBookExcelVo.getExportList())
                    .doWrite(allBookDetailInfo);
        } catch (Exception e) {
            downLoadHandle.handleException(response, e);
        }
    }

    /**
     * 得到首页相关的数量信息
     * */
    @Cacheable(value = "const",key = "'getCountInfo'")
    @GetMapping("/getCountInfo")
    public Result getCountInfo() {
        return bookInfoService.getCountInfo();
    }


    /**
     * 得到首页相关的的图表信息
     * */
    @Cacheable(value = "const",key = "'getBookChartInfo'")
    @GetMapping("/getBookChartInfo")
    public Result getBookChartInfo() {
        return Result.ok().data("bookChartInfo", bookInfoService.getBookChartInfo());
    }


    /**
     * 通过Excel批量添加书籍
     * */
    @CacheEvict(value = "bookInfo", allEntries = true)
    @PreAuthorize("hasAuthority('importBookInfoExcel')")
    @PostMapping("/addBookByExcel")
    public Result uploadUserInfo(MultipartFile file) throws IOException{
        //校验文件类型是否为Excel
        String contentType = file.getContentType();
        if (!"application/vnd.ms-excel".equals(contentType) &&
                !"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(contentType)) {
            return Result.error().message("文件类型不正确，必须为Excel文件");
        }
        //开始Excel导入
        EasyExcel.read(file.getInputStream(), AddBookVo.class, new AddBookExcelListen(bookInfoService,validator)).sheet().doRead();
        return Result.ok();
    }
}
