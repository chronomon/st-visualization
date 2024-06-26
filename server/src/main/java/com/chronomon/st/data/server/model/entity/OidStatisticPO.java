package com.chronomon.st.data.server.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户表：时间片内不同oid的统计信息表
 *
 * @author wangrubin
 */
@Data
@TableName("t_user_gps_oid_statistic")
public class OidStatisticPO {
    /**
     * 时间片起始时间戳
     */
    @TableId(value = "period_start_time", type = IdType.NONE)
    private Long periodStartTime;

    /**
     * 序列化后的统计数据
     */
    private byte[] dataBatch;
}
