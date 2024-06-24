package com.chronomon.st.data.server.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chronomon.st.data.server.model.entity.RawDataPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface RawDataMapper extends BaseMapper<RawDataPO> {
    /**
     * 创建原始数据表
     *
     * @param tableName 表名
     */
    @Update("create table #{tableName} (like t_sys_gps_raw_template including all)")
    int createTable(@Param("tableName") String tableName);

    @Update("drop table #{tableName}")
    int dropTable(@Param("tableName") String tableName);
}
