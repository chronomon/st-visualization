package com.chronomon.st.data.server.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chronomon.st.data.server.model.entity.OidBatchPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface OidBatchMapper extends BaseMapper<OidBatchPO> {
    /**
     * 创建OID + 时间片分组的数据包存储表
     *
     * @param tableName 表名
     */
    @Update("create table #{tableName} (like t_sys_gps_batch_template including all)")
    int createTable(@Param("tableName") String tableName);
}
