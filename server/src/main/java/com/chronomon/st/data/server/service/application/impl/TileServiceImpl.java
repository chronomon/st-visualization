package com.chronomon.st.data.server.service.application.impl;

import com.chronomon.st.data.model.feature.MapFeature;
import com.chronomon.st.data.model.feature.PatchFeature;
import com.chronomon.st.data.model.pyramid.TileMapLocation;
import com.chronomon.st.data.server.catalog.CatalogContext;
import com.chronomon.st.data.server.model.entity.CatalogPO;
import com.chronomon.st.data.server.model.param.TileTemporalQueryParam;
import com.chronomon.st.data.server.service.data.IRawDataService;
import com.chronomon.st.data.server.service.data.ITileBatchService;
import com.chronomon.st.data.server.service.data.ITileStatisticService;
import com.chronomon.st.data.server.service.application.ITileService;
import com.chronomon.st.data.model.statistic.TileStatistic;
import com.chronomon.st.data.server.utils.TileEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TileServiceImpl implements ITileService {

    /**
     * 瓦片中最大位置数据量，如果小于该值则展示真实位置数据，否则展示像素块表示的热力数据
     */
    private static final int MAX_FEATURE_PER_TILE = 5000;

    @Resource
    private IRawDataService rawDataService;

    @Resource
    private ITileBatchService tileBatchService;

    @Resource
    private ITileStatisticService tileStatisticService;

    @Override
    public byte[] getTile(TileTemporalQueryParam param) {
        CatalogPO catalogPO = CatalogContext.getCatalog();

        Instant nextRollPeriod = null;
        Map<Instant, TileStatistic> period2TileStatistic = new HashMap<>();
        if (catalogPO.getNextRollPeriod() <= param.getEndTime().getEpochSecond()) {
            // 查找待归档数据
            nextRollPeriod = Instant.ofEpochSecond(catalogPO.getNextRollPeriod());
            period2TileStatistic.put(nextRollPeriod, rawDataService.getTileStatistic(param.getMinZVal(), param.getMaxZVal()));
        }
        if (catalogPO.getNextRollPeriod() > param.getStartTime().getEpochSecond()) {
            // 查询已归档数据
            period2TileStatistic.putAll(tileStatisticService.getTileStatistic(param));
        }

        // 统计数据总量
        long sum = period2TileStatistic.values().stream()
                .map(TileStatistic::count)
                .reduce(Long::sum).orElse(0L);
        if (param.isMinGrainSize() || sum < MAX_FEATURE_PER_TILE) {
            // 数据量较小，显示真实数据
            List<MapFeature> featureList = new ArrayList<>();
            if (nextRollPeriod != null) {
                // 查询待归档位置数据
                featureList.addAll(rawDataService.getFeaturesByZRange(param.getMinZVal(), param.getMaxZVal()));
                period2TileStatistic.remove(nextRollPeriod);
            }
            // 查询已归档位置数据
            featureList.addAll(tileBatchService.getFeatures(param, period2TileStatistic));

            // 地图瓦片编码
            return dataTile(param, featureList);
        } else {
            // 数据量太大，显示统计数据
            return this.patchTile(param, period2TileStatistic);
        }
    }

    /**
     * 根据位置数据生成位置数据瓦片
     *
     * @param param       查询参数
     * @param featureList 时空范围内的位置数据集
     * @return 位置数据瓦片
     */
    private byte[] dataTile(TileTemporalQueryParam param, List<MapFeature> featureList) {
        // 坐标转换
        int zoomLevelStep = param.getZoomLevelStep();
        featureList.forEach(
                mapFeature -> {
                    mapFeature.mapX = mapFeature.mapX >> zoomLevelStep;
                    mapFeature.mapY = mapFeature.mapY >> zoomLevelStep;
                });

        // 执行矢量瓦片编码
        return TileEncoder.generateDataTile(featureList, param.getTileMapLocation());
    }

    /**
     * 根据统计量生成像素块表示的热力瓦片
     *
     * @param param                查询参数
     * @param period2TileStatistic 统计量
     * @return 像素块瓦片
     */
    private byte[] patchTile(TileTemporalQueryParam param, Map<Instant, TileStatistic> period2TileStatistic) {
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
}
