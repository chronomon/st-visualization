package com.chronomon.st.data.server.service.data;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chronomon.st.data.model.statistic.OidStatistic;
import com.chronomon.st.data.server.model.entity.OidStatisticPO;

import java.time.Instant;
import java.util.Map;

public interface IOidStatisticService extends IService<OidStatisticPO> {

    void createTable(String catalogId);

    void dropTable(String catalogId);

    boolean saveStatistic(Map<Instant, OidStatistic> periodStartTime2OidStatisticMap);

    OidStatistic getStatistic(Instant periodStartTime);
}
