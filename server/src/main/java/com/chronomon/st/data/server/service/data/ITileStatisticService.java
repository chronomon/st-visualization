package com.chronomon.st.data.server.service.data;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chronomon.st.data.server.model.entity.TileStatisticPO;
import com.chronomon.st.data.server.model.param.TileTemporalQueryParam;
import com.chronomon.st.data.model.statistic.TileStatistic;

import java.time.Instant;
import java.util.Map;

public interface ITileStatisticService extends IService<TileStatisticPO> {

    boolean createTable(String catalogId);

    boolean dropTable(String catalogId);

    Map<Instant, TileStatistic> getTileStatistic(TileTemporalQueryParam param);

    byte[] patchTile(TileTemporalQueryParam param, Map<Instant, TileStatistic> period2TileStatistic);
}
