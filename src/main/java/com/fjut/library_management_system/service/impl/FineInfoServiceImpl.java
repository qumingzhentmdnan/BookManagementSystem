package com.fjut.library_management_system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fjut.library_management_system.entity.FineInfo;
import com.fjut.library_management_system.mapper.FineInfoMapper;
import com.fjut.library_management_system.service.FineInfoService;
import com.fjut.library_management_system.util.VirtualThreadUtil;
import com.fjut.library_management_system.vo.FineVo;
import com.fjut.library_management_system.vo.QueryFineVo;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 叶良辰
 * @since 2024年03月31日
 */
@Service
public class FineInfoServiceImpl extends ServiceImpl<FineInfoMapper, FineInfo> implements FineInfoService {
   @Resource
   private FineInfoMapper fineInfoMapper;

   //根据条件查询罚款信息
    @Override
    public List<FineVo> getFineInfo(QueryFineVo queryFineVo){
        if(queryFineVo.getPage()==null||queryFineVo.getLimit()==null){
            return VirtualThreadUtil.executor(()->fineInfoMapper.getFineInfo(queryFineVo, null));
        }else{
            return VirtualThreadUtil.executor(()->fineInfoMapper.getFineInfo(queryFineVo,  ((long) (queryFineVo.getPage() - 1) *queryFineVo.getLimit())));
        }
    }

    //得到满足条件的罚款数量，用户分页
    @Override
    public Long getFineCount(QueryFineVo queryFineVo) {
        return VirtualThreadUtil.executor(()->fineInfoMapper.getFineCount(queryFineVo));
    }


}
