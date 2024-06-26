package com.chronomon.st.data.server.service.data.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chronomon.st.data.model.statistic.OidStatistic;
import com.chronomon.st.data.server.model.entity.OidStatisticPO;
import com.chronomon.st.data.server.dao.OidStatisticMapper;
import com.chronomon.st.data.server.service.data.IOidStatisticService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OidStatisticServiceImpl extends ServiceImpl<OidStatisticMapper, OidStatisticPO> implements IOidStatisticService {

    // 模板表名
    private static final String TEMPLATE_TABLE = "t_template_gps_statistic";
    // 用户表名前缀
    private static final String USER_TABLE_PREFIX = "t_user_gps_oid_statistic_";

    @Resource
    private DynamicTableService dynamicTableService;

    @Override
    public void createTable(String catalogId) {
        dynamicTableService.createTable(USER_TABLE_PREFIX + catalogId, TEMPLATE_TABLE);
    }

    @Override
    public void dropTable(String catalogId) {
        dynamicTableService.dropTable(USER_TABLE_PREFIX + catalogId);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean saveStatistic(Map<Instant, OidStatistic> periodStartTime2OidStatisticMap) {
        List<OidStatisticPO> tileStatisticPOList = periodStartTime2OidStatisticMap.entrySet().stream()
                .map(periodStartTime2OidStatistic -> {
                    OidStatisticPO tileStatisticPO = new OidStatisticPO();
                    tileStatisticPO.setPeriodStartTime(periodStartTime2OidStatistic.getKey().getEpochSecond());
                    tileStatisticPO.setDataBatch(periodStartTime2OidStatistic.getValue().serialize());
                    return tileStatisticPO;
                }).collect(Collectors.toList());
        return saveBatch(tileStatisticPOList);
    }

    @Override
    public OidStatistic getStatistic(Instant periodStartTime) {
        return new OidStatistic(getById(periodStartTime).getDataBatch());
    }
}
