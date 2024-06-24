package com.chronomon.st.data.server.service.data;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chronomon.st.data.model.collection.PeriodTileCollection;
import com.chronomon.st.data.server.model.entity.TileBatchPO;
import com.chronomon.st.data.server.model.param.TileTemporalQueryParam;
import com.chronomon.st.data.model.statistic.TileStatistic;

import java.time.Instant;
import java.util.Map;

public interface ITileBatchService extends IService<TileBatchPO> {

    boolean createTable(String catalogId);

    boolean dropTable(String catalogId);

    boolean saveFeatures(PeriodTileCollection tileCollection);

    byte[] dataTile(TileTemporalQueryParam param, Map<Instant, TileStatistic> period2TileStatistic);
}
