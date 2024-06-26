package com.chronomon.st.data.server.service.data;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chronomon.st.data.server.model.entity.TileStatisticPO;
import com.chronomon.st.data.server.model.param.TileTemporalQueryParam;
import com.chronomon.st.data.model.statistic.TileStatistic;

import java.time.Instant;
import java.util.Map;

public interface ITileStatisticService extends IService<TileStatisticPO> {

    void createTable(String catalogId);

    void dropTable(String catalogId);

    boolean saveStatistic(Map<Instant, TileStatistic> periodStartTime2TileStatisticMap);

    Map<Instant, TileStatistic> getTileStatistic(TileTemporalQueryParam param);
}
