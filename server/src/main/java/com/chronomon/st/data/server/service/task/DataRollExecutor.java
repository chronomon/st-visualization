package com.chronomon.st.data.server.service.task;

import com.chronomon.st.data.model.collection.PeriodCollection;
import com.chronomon.st.data.model.collection.PeriodOidCollection;
import com.chronomon.st.data.model.collection.PeriodTileCollection;
import com.chronomon.st.data.model.feature.MapFeature;
import com.chronomon.st.data.model.pyramid.MapDescriptor;
import com.chronomon.st.data.model.statistic.OidStatistic;
import com.chronomon.st.data.model.statistic.TileStatistic;
import com.chronomon.st.data.server.catalog.CatalogContext;
import com.chronomon.st.data.server.model.entity.CatalogPO;
import com.chronomon.st.data.server.service.catalog.ICatalogService;
import com.chronomon.st.data.server.service.data.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 将时空对象数据打包归档的异步任务
 *
 * @author wangrubin
 */
@Slf4j
@Service
public class DataRollExecutor {

    @Resource
    private DataRollScheduler dataRollScheduler;

    @Resource
    private ICatalogService catalogService;

    @Resource
    private IRawDataService rawDataService;

    @Resource
    private ITileBatchService tileBatchService;

    @Resource
    private IOidBatchService oidBatchService;

    @Resource
    private ITileStatisticService tileStatisticService;

    @Resource
    private IOidStatisticService oidStatisticService;

    /**
     * 按照时间片归档原始位置数据为数据包
     */
    @Transactional(rollbackFor = Exception.class)
    @Async("taskExecutor")
    public void rollData(String catalogId, long periodUntilTime) {

        CatalogPO catalogPO = catalogService.getCatalogById(catalogId);
        if (catalogPO.getNextRollPeriod() >= periodUntilTime) {
            // 说明对应时间片的数据已经被之前的触发归档，无需再归档
            return;
        }

        // 设置当前线程的用户目录：为了让表名替换生效
        CatalogContext.saveCatalog(catalogPO);
        try {
            // 查询原始位置数据
            List<MapFeature> mapFeatureList = rawDataService.getFeaturesByUntilTime(periodUntilTime);

            // 原始位置数据归档
            MapDescriptor mapDescriptor = new MapDescriptor(catalogPO.getMaxZoomLevel(), catalogPO.getTileExtent());
            rollData(mapFeatureList, mapDescriptor);

            // 原始位置数据删除
            rawDataService.removeByTime(periodUntilTime);

            // 更新用户目录中的最后一次归档时间片
            catalogPO.setNextRollPeriod(periodUntilTime);
            catalogService.updateCatalog(catalogPO);
        } catch (Exception e) {
            // 归档失败，将当前归档时间片，继续注册到归档调度器中
            dataRollScheduler.registerTrigger(catalogId, periodUntilTime);
            log.error("原始位置数据归档失败: " + catalogId + "->" + periodUntilTime, e);
            throw e;
        } finally {
            CatalogContext.removeCatalog();
        }
    }

    /**
     * 将当条存储的位置数据归档成打包存储
     *
     * @param mapFeatureList 位置数据集合
     * @param mapDescriptor  电子地图信息
     */
    private void rollData(List<MapFeature> mapFeatureList, MapDescriptor mapDescriptor) {
        System.out.println("同步数据条数：" + mapFeatureList.size());
        // 1. 根据时间片分组
        List<PeriodCollection> periodCollectionList = PeriodCollection.groupByPeriod(mapFeatureList, ChronoUnit.HOURS);

        // 2.1 时间片内的数据再根据OID分组
        List<PeriodOidCollection> periodOidCollectionList = periodCollectionList.stream()
                .flatMap(periodCollection -> periodCollection.groupByOid().stream())
                .collect(Collectors.toList());

        // 2.2 时间片内的数据再根据瓦片分组
        List<PeriodTileCollection> periodTileCollectionList = periodCollectionList.stream()
                .flatMap(periodCollection -> periodCollection.groupByTile(mapDescriptor).stream())
                .collect(Collectors.toList());

        // 3.1 时间片内不同OID的统计量
        Map<Instant, OidStatistic> period2OidStatisticMap = periodCollectionList.stream()
                .collect(Collectors.toMap(
                        collection -> collection.periodStartTime,
                        PeriodCollection::getOidStatistic)
                );

        // 3.2 时间片内不同瓦片的统计量
        Map<Instant, TileStatistic> period2TileStatisticMap = periodCollectionList.stream()
                .collect(Collectors.toMap(
                        collection -> collection.periodStartTime,
                        PeriodCollection::getTileStatistic)
                );

        // 4. 将数据包和统计量入库
        tileBatchService.saveFeatures(periodTileCollectionList);
        oidBatchService.saveFeatures(periodOidCollectionList);
        oidStatisticService.saveStatistic(period2OidStatisticMap);
        tileStatisticService.saveStatistic(period2TileStatisticMap);
    }
}
