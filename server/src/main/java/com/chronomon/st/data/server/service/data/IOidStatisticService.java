package com.chronomon.st.data.server.service.data;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chronomon.st.data.server.model.entity.OidStatisticPO;

import java.time.Instant;

public interface IOidStatisticService extends IService<OidStatisticPO> {

    boolean createTable(String catalogName);

    OidStatisticPO getStatistic(Instant periodStartTime);
}
