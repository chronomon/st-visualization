package com.chronomon.st.data.server.service.parallel;

import com.chronomon.st.data.model.collection.PeriodCollection;
import com.chronomon.st.data.model.collection.PeriodOidCollection;
import com.chronomon.st.data.model.collection.PeriodTileCollection;
import com.chronomon.st.data.model.feature.MapFeature;
import com.chronomon.st.data.model.pyramid.MapDescriptor;
import com.chronomon.st.data.server.model.entity.CatalogPO;
import com.chronomon.st.data.server.service.data.IRawDataService;
import com.chronomon.st.data.server.service.data.ITileBatchService;
import com.chronomon.st.data.server.service.data.ITileStatisticService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Future;

/**
 * 将时空对象数据打包归档的定时任务
 *
 * @author wangrubin
 */
@Service
public class DataRollTaskService {

    @Resource
    private IRawDataService rawDataService;

    @Resource
    private ITileBatchService tileBatchService;

    @Resource
    private ITileStatisticService tileStatisticService;

    /**
     * 每分钟归档数据的定时任务
     */
    @Async("taskExecutor")
    public Future<Boolean> rollData(CatalogPO catalogPO, Instant periodStartTime, Instant periodEndTime) {
        // 查询当前用户目录下的原始时空对象
        List<MapFeature> featureList = rawDataService.getFeatures(catalogPO.getCatalogId(), periodStartTime, periodEndTime);
        PeriodCollection periodCollection = new PeriodCollection(periodStartTime, featureList);

        // 按照瓦片和oid分组
        MapDescriptor mapDescriptor = new MapDescriptor(catalogPO.getMaxZoomLevel(), catalogPO.getTileExtent());
        List<PeriodTileCollection> periodTileCollectionList = periodCollection.groupByTile(mapDescriptor);
        List<PeriodOidCollection> periodOidCollectionList = periodCollection.groupByOid();

        // 存储分组后的数据包和数据统计量


    }
}
