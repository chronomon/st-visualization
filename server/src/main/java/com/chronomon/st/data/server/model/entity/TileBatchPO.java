package com.chronomon.st.data.server.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chronomon.st.data.model.collection.PeriodTileCollection;
import com.chronomon.st.data.model.pyramid.TileMapLocation;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 系统表：用户目录元数据
 *
 * @author wangrubin
 */
@Data
@NoArgsConstructor
@TableName("t_user_gps_tile_batch")
public class TileBatchPO {
    /**
     * 空间 + 时间索引: zVal + periodStartSeconds
     */
    @TableId(value = "combine_index", type = IdType.NONE)
    private String combineIndex;

    /**
     * 压缩后的数据包
     */
    private byte[] dataBatch;

    public TileBatchPO(PeriodTileCollection periodTileCollection) {
        this.combineIndex = periodTileCollection.tileLocation.zCurveCode() + "_" + periodTileCollection.periodStartTime.getEpochSecond();
        this.dataBatch = periodTileCollection.serializeFeatures();
    }

    public PeriodTileCollection toCollection(int tileExtent) {
        String[] zValAndPeriodTime = this.combineIndex.split("_");
        long zVal = (Long.parseLong(zValAndPeriodTime[0]));
        Instant periodStartTime = Instant.ofEpochSecond(Long.parseLong(zValAndPeriodTime[1]));
        return new PeriodTileCollection(periodStartTime, new TileMapLocation(zVal, tileExtent), this.dataBatch);
    }
}
