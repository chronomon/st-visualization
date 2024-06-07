package com.chronomon.st.data.server.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chronomon.st.data.server.model.entity.CatalogPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CatalogMapper extends BaseMapper<CatalogPO> {
    /**
     * 创建原始数据表
     *
     * @param tableName 表名
     */
    int createTable(@Param("tableName") String tableName);
}
