package com.chronomon.st.data.server.service.data.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chronomon.st.data.model.statistic.TileStatistic;
import com.chronomon.st.data.server.catalog.CatalogContext;
import com.chronomon.st.data.server.dao.TileStatisticMapper;
import com.chronomon.st.data.server.model.entity.CatalogPO;
import com.chronomon.st.data.server.model.entity.TileStatisticPO;
import com.chronomon.st.data.server.model.param.TileTemporalQueryParam;
import com.chronomon.st.data.server.service.data.ITileStatisticService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class TileStatisticServiceImpl extends ServiceImpl<TileStatisticMapper, TileStatisticPO> implements ITileStatisticService {

    private static final long MAX_CACHE_COUNT = 500;

    private static final int EXPIRE_MINUTES = 30;

    private static final Cache<String, TileStatistic> STATISTIC_CACHE =
            Caffeine.newBuilder()
                    .expireAfterAccess(EXPIRE_MINUTES, TimeUnit.MINUTES)
                    .maximumSize(MAX_CACHE_COUNT).build();

    // 模板表名
    private static final String TEMPLATE_TABLE = "t_template_gps_statistic";

    // 用户表名前缀
    private static final String USER_TABLE_PREFIX = "t_user_gps_tile_statistic_";

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
    public boolean saveStatistic(Map<Instant, TileStatistic> periodStartTime2TileStatisticMap) {
        List<TileStatisticPO> tileStatisticPOList = periodStartTime2TileStatisticMap.entrySet().stream()
                .map(periodStartTime2TileStatistic -> {
                    TileStatisticPO tileStatisticPO = new TileStatisticPO();
                    tileStatisticPO.setPeriodStartTime(periodStartTime2TileStatistic.getKey().getEpochSecond());
                    tileStatisticPO.setDataBatch(periodStartTime2TileStatistic.getValue().serialize());
                    return tileStatisticPO;
                }).collect(Collectors.toList());
        return saveBatch(tileStatisticPOList);
    }

    @Override
    public Map<Instant, TileStatistic> getTileStatistic(TileTemporalQueryParam param) {
        // 先根据时间范围查询统计信息
        Map<Instant, TileStatistic> period2TileStatisticMap = queryTileStatistic(param);

        // 再根据空间范围过滤统计信息
        long minZVal = param.getMinZVal();
        long maxZVal = param.getMaxZVal();
        for (Map.Entry<Instant, TileStatistic> period2TileStatistic : period2TileStatisticMap.entrySet()) {
            period2TileStatistic.setValue(period2TileStatistic.getValue().filter(minZVal, maxZVal));
        }

        // 返回满足时空过滤条件的统计量
        return period2TileStatisticMap;
    }

    private Map<Instant, TileStatistic> queryTileStatistic(TileTemporalQueryParam param) {
        Map<Instant, TileStatistic> period2TileStatistic = new HashMap<>();
        List<Long> missedPeriodList = new ArrayList<>();
        CatalogPO catalogPO = CatalogContext.getCatalog();
        for (Instant periodTime : param.getPeriodTimeList()) {
            String cacheKey = concatCacheKey(periodTime.getEpochSecond(), catalogPO.getCatalogId());
            TileStatistic tileStatistic = STATISTIC_CACHE.getIfPresent(cacheKey);
            if (tileStatistic == null) {
                // 缓存中没有命中统计量
                missedPeriodList.add(periodTime.getEpochSecond());
            } else {
                // 缓存中命中了统计量
                period2TileStatistic.put(periodTime, tileStatistic);
            }
        }

        // 获取缓存中没有的统计量, 从数据库中查询
        LambdaQueryWrapper<TileStatisticPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(TileStatisticPO::getPeriodStartTime, missedPeriodList);
        List<TileStatisticPO> missedStatisticPOList = list(queryWrapper);
        for (TileStatisticPO missedTileStatisticPO : missedStatisticPOList) {
            String cacheKey = concatCacheKey(missedTileStatisticPO.getPeriodStartTime(), catalogPO.getCatalogId());
            TileStatistic tileStatistic = new TileStatistic(missedTileStatisticPO.getDataBatch());
            period2TileStatistic.put(Instant.ofEpochSecond(missedTileStatisticPO.getPeriodStartTime()), tileStatistic);
            STATISTIC_CACHE.put(cacheKey, tileStatistic);
        }

        return period2TileStatistic;
    }

    /**
     * 缓存Key由时间片 + 用户目录组成
     *
     * @param periodTimeSeconds 时间片起始时刻
     * @param catalogId         用户目录Key
     * @return 缓存Key
     */
    private String concatCacheKey(long periodTimeSeconds, String catalogId) {
        return String.format("%s_%s", periodTimeSeconds, catalogId);
    }
}
