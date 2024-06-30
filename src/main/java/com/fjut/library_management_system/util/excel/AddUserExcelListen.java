package com.fjut.library_management_system.util.excel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.alibaba.fastjson2.JSON;
import com.fjut.library_management_system.exception.CustomizedException;
import com.fjut.library_management_system.service.UserService;
import com.fjut.library_management_system.util.Result;
import com.fjut.library_management_system.util.SecurityUtil;
import com.fjut.library_management_system.util.VirtualThreadUtil;
import com.fjut.library_management_system.vo.AddUserVo;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.*;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.util.List;


//Excel批量导入用户信息监听器
@Slf4j
public class AddUserExcelListen implements ReadListener<AddUserVo> {
    Logger logger = LoggerFactory.getLogger("com.fjut.operation");
    //每100条储存一次
    private static final int BATCH_COUNT = 100;
    //缓存的数据
    private List<AddUserVo> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

    private UserService userService;
    private Validator validator;
    private AddUserVo currentUser;

    //构造函数
    public AddUserExcelListen(UserService userService,Validator validator) {
        this.userService = userService;
        this.validator=validator;
    }

    /**
     * 这个每一条数据解析都会来调用
     */
    @Override
    public void invoke(@Validated AddUserVo data, AnalysisContext context) {
        log.info("解析到一条数据:{}", JSON.toJSONString(data));
        // 校验数据
        Errors errors = new BeanPropertyBindingResult(data, "data");
        validator.validate(data,errors);
        // 如果校验不通过,抛出异常，拼接错误信息
        try {
            ReadExcelHandle.onValidationHasErrors(errors,context,currentUser);
        } catch (IOException e) {
            throw new CustomizedException(20001,e.getMessage());
        }
        //加入缓存
        cachedDataList.add(data);
        // 达到BATCH_COUNT了，需要去存储一次数据库，防止数据几万条数据在内存，容易OOM
        if (cachedDataList.size() >= BATCH_COUNT) {
            saveData();
            //保存完成后，设置当前储存到的位置
            currentUser=cachedDataList.getLast();
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
        log.info("所有数据解析完成！");
    }

    /**
     * 加上存储数据库
     */
    private void saveData() {
        if(cachedDataList.isEmpty())
            return;
        Result result = userService.addUser(cachedDataList);
        if(result.getCode()!=20000){
            try {
                ReadExcelHandle.onSavaException(currentUser,result);
            } catch (IOException e) {
                throw new CustomizedException(20001,e.getMessage());
            }
        }
        logger.info("用户{}执行了{}方法，参数为{}", SecurityUtil.getCurrentUsername(), "uploadUserInfo", JSON.toJSONString(cachedDataList));
    }

    //异常处理
    @Override
    public void onException(Exception exception, AnalysisContext context) throws Exception {
        ReadExcelHandle.onReadException(exception,currentUser);
    }
}