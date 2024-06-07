package com.chronomon.st.data.server.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chronomon.st.data.server.model.entity.OidStatisticPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface OidStatisticMapper extends BaseMapper<OidStatisticPO> {

    @Update("create table #{tableName} (like t_sys_gps_statistic_template including all)")
    int createTable(@Param("tableName") String tableName);
}
