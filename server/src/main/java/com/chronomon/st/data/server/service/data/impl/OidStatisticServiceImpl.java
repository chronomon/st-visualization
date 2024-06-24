package com.chronomon.st.data.server.service.data.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chronomon.st.data.server.model.entity.OidStatisticPO;
import com.chronomon.st.data.server.dao.OidStatisticMapper;
import com.chronomon.st.data.server.service.data.IOidStatisticService;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class OidStatisticServiceImpl extends ServiceImpl<OidStatisticMapper, OidStatisticPO> implements IOidStatisticService {
    @Override
    public boolean createTable(String catalogId) {
        return getBaseMapper().createTable("t_user_gps_oid_statistic" + "_" + catalogId) > 0;
    }

    @Override
    public boolean dropTable(String catalogId) {
        return getBaseMapper().dropTable("t_user_gps_oid_statistic" + "_" + catalogId) > 0;
    }

    @Override
    public OidStatisticPO getStatistic(Instant periodStartTime) {
        return getById(periodStartTime);
    }
}
