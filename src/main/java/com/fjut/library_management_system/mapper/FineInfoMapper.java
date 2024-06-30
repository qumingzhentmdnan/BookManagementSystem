package com.fjut.library_management_system.mapper;

import com.fjut.library_management_system.entity.FineInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fjut.library_management_system.vo.FineVo;
import com.fjut.library_management_system.vo.QueryFineVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author 叶良辰
 * @since 2024年03月31日
 */
@Mapper
public interface FineInfoMapper extends BaseMapper<FineInfo> {
    List<FineVo> getFineInfo(@Param("queryFineVo")QueryFineVo queryFineVo,Long page);

    Long getFineCount(@Param("queryFineVo")QueryFineVo queryFineVo);

    int queryUserHasFies(Long userId);
}
