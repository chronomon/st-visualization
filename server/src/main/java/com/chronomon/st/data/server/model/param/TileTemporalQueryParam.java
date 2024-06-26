package com.chronomon.st.data.server.model.param;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.chronomon.st.data.server.model.entity.CatalogPO;
import com.chronomon.st.data.model.pyramid.TileMapLocation;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 空间 + 时间范围查询参数
 *
 * @author wangrubin
 */
@Data
@NoArgsConstructor
public class TileTemporalQueryParam extends TemporalParam {

    private int zoomLevel;

    private int columnNum;

    private int rowNum;

    @JsonIgnore
    private int maxZoomLevel;

    @JsonIgnore
    private int tileExtent;

    @JsonIgnore
    private long minZVal;

    @JsonIgnore
    private long maxZVal;

    public TileTemporalQueryParam(Instant startTime, Instant endTime,
                                  int zoomLevel, int columnNum, int rowNum) {
        super(startTime, endTime);
        this.zoomLevel = zoomLevel;
        this.columnNum = columnNum;
        this.rowNum = rowNum;
    }

    @Override
    public void bindCatalog(CatalogPO catalogPO) {
        super.bindCatalog(catalogPO);

        assert catalogPO.getMaxZoomLevel() >= this.zoomLevel : "层级不合法";
        this.maxZoomLevel = catalogPO.getMaxZoomLevel();
        this.tileExtent = catalogPO.getTileExtent();

        long scale = (long) Math.pow(4, catalogPO.getMaxZoomLevel() - this.zoomLevel);
        this.minZVal = TileMapLocation.zCurveEncode(columnNum, rowNum) * scale;
        this.maxZVal = minZVal + scale - 1;
    }

    public TileMapLocation getTileMapLocation() {
        return new TileMapLocation(columnNum, rowNum, tileExtent);
    }

    public int getZoomLevelStep() {
        return this.maxZoomLevel - this.zoomLevel;
    }

    @Override
    public boolean isMinGrainSize() {
        return super.isMinGrainSize() && minZVal == maxZVal;
    }
}
