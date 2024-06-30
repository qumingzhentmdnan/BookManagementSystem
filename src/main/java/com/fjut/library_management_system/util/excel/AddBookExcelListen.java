package com.fjut.library_management_system.util.excel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.alibaba.fastjson2.JSON;
import com.fjut.library_management_system.exception.CustomizedException;
import com.fjut.library_management_system.service.BookInfoService;
import com.fjut.library_management_system.util.Result;
import com.fjut.library_management_system.util.SecurityUtil;
import com.fjut.library_management_system.util.VirtualThreadUtil;
import com.fjut.library_management_system.vo.AddBookVo;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.*;

import java.io.IOException;
import java.util.List;

//Excel批量导入书籍信息监听器
public class AddBookExcelListen implements ReadListener<AddBookVo> {
    Logger logger = LoggerFactory.getLogger("com.fjut.operation");

    //每100条储存一次
    private static final int BATCH_COUNT = 100;

    //缓存未插入的数据
    private List<AddBookVo> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

    private BookInfoService bookInfoService;
    private Validator validator;

    //当前正在插入到的数据
    private AddBookVo currentBook;

    //构造函数
    public AddBookExcelListen(BookInfoService bookInfoService,Validator validator) {
        this.bookInfoService = bookInfoService;
        this.validator=validator;
    }

    /**
     * 这个每一条数据解析都会来调用
     */
    @Override
    public void invoke(AddBookVo data, AnalysisContext context) {
        // 校验数据
        Errors errors = new BeanPropertyBindingResult(data, "data");
        validator.validate(data,errors);
        try {
            ReadExcelHandle.onValidationHasErrors(errors,context,currentBook);
        } catch (IOException e) {
            throw new CustomizedException(20001,e.getMessage());
        }

        //加入缓存
        cachedDataList.add(data);

        // 达到BATCH_COUNT了，需要去存储一次数据库，防止数据几万条数据在内存，容易OOM
        if (cachedDataList.size() >= BATCH_COUNT) {
            saveData();
            //保存完成后，设置当前储存到的位置
            currentBook =cachedDataList.getLast();
            // 存储完成清理 list
            cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
        }
    }

    /**
     * 所有数据解析完成了 都会来调用
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 这里也要保存数据，确保最后遗留的数据也存储到数据库
        saveData();
    }

    /**
     * 存储到数据库
     */
    private void saveData() {
        if (cachedDataList.isEmpty())
            return;
        Result result =bookInfoService.addBookInfo(cachedDataList);
        if(result.getCode()!=20000){
            try {
                ReadExcelHandle.onSavaException(currentBook,result);
            } catch (IOException e) {
                throw new CustomizedException(20001,e.getMessage());
            }
        }
        logger.info("用户{}执行了{}方法，参数为{}", SecurityUtil.getCurrentUsername(), "uploadBookInfo", JSON.toJSONString(cachedDataList));
    }

    //异常处理
    @Override
    public void onException(Exception exception, AnalysisContext context) throws Exception {
        ReadExcelHandle.onReadException(exception,currentBook);
    }
}