package com.fjut.library_management_system.service;

import com.fjut.library_management_system.entity.FineInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fjut.library_management_system.vo.FineVo;
import com.fjut.library_management_system.vo.QueryFineVo;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 叶良辰
 * @since 2024年03月31日
 */
public interface FineInfoService  extends IService<FineInfo>{
    List<FineVo> getFineInfo(QueryFineVo queryFineVo);

    Long getFineCount(QueryFineVo queryFineVo);
}
