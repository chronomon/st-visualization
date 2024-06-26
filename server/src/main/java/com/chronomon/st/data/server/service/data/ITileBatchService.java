package com.chronomon.st.data.server.service.data;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chronomon.st.data.model.collection.PeriodTileCollection;
import com.chronomon.st.data.model.feature.MapFeature;
import com.chronomon.st.data.model.statistic.TileStatistic;
import com.chronomon.st.data.server.model.entity.TileBatchPO;
import com.chronomon.st.data.server.model.param.TileTemporalQueryParam;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface ITileBatchService extends IService<TileBatchPO> {

    void createTable(String catalogId);

    void dropTable(String catalogId);

    boolean saveFeatures(List<PeriodTileCollection> tileCollectionList);

    List<MapFeature> getFeatures(TileTemporalQueryParam param, Map<Instant, TileStatistic> period2TileStatistic);
}
