package com.chronomon.st.data.server.service.data.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chronomon.st.data.server.catalog.CatalogContext;
import com.chronomon.st.data.server.model.entity.CatalogPO;
import com.chronomon.st.data.server.model.entity.TileStatisticPO;
import com.chronomon.st.data.server.model.param.TileTemporalQueryParam;
import com.chronomon.st.data.server.utils.TileEncoder;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.chronomon.st.data.server.dao.TileStatisticMapper;
import com.chronomon.st.data.server.service.data.ITileStatisticService;
import com.chronomon.st.data.model.feature.PatchFeature;
import com.chronomon.st.data.model.pyramid.TileMapLocation;
import com.chronomon.st.data.model.statistic.TileStatistic;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class TileStatisticServiceImpl extends ServiceImpl<TileStatisticMapper, TileStatisticPO> implements ITileStatisticService {

    private static final long MAX_CACHE_COUNT = 500;

    private static final Cache<String, TileStatistic> STATISTIC_CACHE =
            Caffeine.newBuilder()
                    .expireAfterAccess(30, TimeUnit.MINUTES)
                    .maximumSize(MAX_CACHE_COUNT).build();

    @Override
    public boolean createTable(String catalogName) {
        return getBaseMapper().createTable("t_user_gps_tile_statistic" + "_" + catalogName) > 0;
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

    @Override
    public byte[] patchTile(TileTemporalQueryParam param, Map<Instant, TileStatistic> period2TileStatistic) {
        // 计算像素块所在层级
        int tileStep = (int) (Math.log(param.getTileExtent()) / Math.log(2));
        int patchZoomLevel = Math.min(param.getZoomLevel() + tileStep, param.getMaxZoomLevel());
        int patchZoomLevelStep = param.getMaxZoomLevel() - patchZoomLevel;

        // 聚合不同像素块中的数据量
        Map<Long, Long> subZ2CountMap = new HashMap<>();
        for (TileStatistic tileStatistic : period2TileStatistic.values()) {
            for (Map.Entry<Long, Integer> zValAndCount : tileStatistic.getzVal2Count().entrySet()) {
                long zVal = zValAndCount.getKey() >> (patchZoomLevelStep * 2);
                subZ2CountMap.merge(zVal, (long) zValAndCount.getValue(), Long::sum);
            }
        }

        // 生成targetLevel级的绝对坐标像素块
        List<PatchFeature> featureList = new ArrayList<>();
        int tileZoomLevelStep = patchZoomLevel - param.getZoomLevel();
        for (Map.Entry<Long, Long> zValAndCount : subZ2CountMap.entrySet()) {
            int[] columnAndRowNum = TileMapLocation.zCurveDecode(zValAndCount.getKey());
            int patchColumnNum = columnAndRowNum[0];
            int patchRowNum = columnAndRowNum[1];

            long patchMinMapX = (long) patchColumnNum * param.getTileExtent();
            long pathMaxMapX = patchMinMapX + param.getTileExtent();
            long patchMinMapY = (long) patchRowNum * param.getTileExtent();
            long patchMaxMapY = patchMinMapY + param.getTileExtent();

            PatchFeature patchFeature = new PatchFeature(zValAndCount.getValue());
            patchFeature.minMapX = patchMinMapX >> tileZoomLevelStep;
            patchFeature.maxMapX = pathMaxMapX >> tileZoomLevelStep;
            patchFeature.minMapY = patchMinMapY >> tileZoomLevelStep;
            patchFeature.maxMapY = patchMaxMapY >> tileZoomLevelStep;

            featureList.add(patchFeature);
        }

        // 矢量瓦片编码
        return TileEncoder.generatePatchTile(featureList, param.getTileMapLocation());
    }

    private Map<Instant, TileStatistic> queryTileStatistic(TileTemporalQueryParam param) {
        Map<Instant, TileStatistic> period2TileStatistic = new HashMap<>();
        List<Instant> missedPeriodList = new ArrayList<>();
        CatalogPO catalogPO = CatalogContext.getCatalog();
        for (Instant periodTime : param.getPeriodTimeList()) {
            String cacheKey = concatCacheKey(periodTime.getEpochSecond(), catalogPO.getAccessKey());
            TileStatistic tileStatistic = STATISTIC_CACHE.getIfPresent(cacheKey);
            if (tileStatistic == null) {
                // 缓存中没有命中统计量
                missedPeriodList.add(periodTime);
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
            String cacheKey = concatCacheKey(missedTileStatisticPO.getPeriodStartTime(), catalogPO.getAccessKey());
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
     * @param accessKey         用户目录Key
     * @return 缓存Key
     */
    private String concatCacheKey(long periodTimeSeconds, String accessKey) {
        return String.format("%s_%s", periodTimeSeconds, accessKey);
    }
}
