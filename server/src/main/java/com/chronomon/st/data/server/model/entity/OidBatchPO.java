package com.chronomon.st.data.server.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chronomon.st.data.model.collection.PeriodOidCollection;
import com.chronomon.st.data.model.collection.PeriodTileCollection;
import com.chronomon.st.data.model.pyramid.TileMapLocation;
import lombok.Data;

import java.time.Instant;

/**
 * 系统表：用户目录元数据
 *
 * @author wangrubin
 */
@Data
@TableName("t_user_gps_oid_batch")
public class OidBatchPO {
    /**
     * OID+时间索引: oid + periodStartSeconds
     */
    @TableId(value = "combine_index", type = IdType.NONE)
    private String combineIndex;

    /**
     * 压缩后的数据包
     */
    private byte[] dataBatch;

    public PeriodOidCollection toCollection() {
        String[] oidAndPeriodTime = this.combineIndex.split("_");
        String oid = oidAndPeriodTime[0];
        Instant periodStartTime = Instant.ofEpochSecond(Long.parseLong(oidAndPeriodTime[1]));
        return new PeriodOidCollection(periodStartTime, oid, this.dataBatch);
    }
}
