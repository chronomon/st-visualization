package com.chronomon.st.data.server.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chronomon.st.data.server.model.entity.RawDataPO;
import lombok.Data;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface RawDataMapper extends BaseMapper<RawDataPO> {
    /**
     * 统计每个瓦片中的数据量
     *
     * @param tableName 表名
     * @param minZVal   最小zVal
     * @param maxZVal   最大zVal
     * @return 每个zVal对应的数量
     */
    @Select("select z_val, count(*) from t_user_gps_raw where z_val between #{minZVal} and #{maxZVal} group by z_val")
    List<ZValAndCount> statistic(@Param("tableName") String tableName,
                                 @Param("minZVal") Long minZVal,
                                 @Param("maxZVal") Long maxZVal);


    @Data
    public static class ZValAndCount{
        private Long zVal;

        private Integer count;
    }
}
