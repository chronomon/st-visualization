package com.chronomon.st.data.server.service.parallel;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chronomon.st.data.model.collection.PeriodCollection;
import com.chronomon.st.data.model.feature.MapFeature;
import com.chronomon.st.data.server.model.entity.CatalogPO;
import com.chronomon.st.data.server.service.catalog.ICatalogService;
import com.chronomon.st.data.server.service.data.IRawDataService;
import com.chronomon.st.data.server.service.data.ITileBatchService;
import com.chronomon.st.data.server.service.data.ITileStatisticService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 将时空对象数据打包归档的定时任务
 *
 * @author wangrubin
 */
@Service
public class DataRollScheduledService {

    @Resource
    private ICatalogService catalogService;

    @Resource
    private IRawDataService rawDataService;

    @Resource
    private ITileBatchService tileBatchService;

    @Resource
    private ITileStatisticService tileStatisticService;

    /**
     * 每分钟归档数据的定时任务
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void rollData() {
        // 明确时间片起止时刻
        Instant periodStartTime = Instant.now().truncatedTo(ChronoUnit.MINUTES);
        Instant periodEndTime = periodStartTime.plusSeconds(ChronoUnit.MINUTES.getDuration().getSeconds());

        // 查询所有时间片跨度为"分钟"的用户目录
        List<CatalogPO> catalogPOList = getCatalogsByPeriodUnit(ChronoUnit.MINUTES);
        List<MapFeature> featureList = rawDataService.getFeatures(periodStartTime, periodEndTime);

        PeriodCollection periodCollection = new PeriodCollection(periodStartTime, featureList);
        periodCollection.groupByTile()

    }

    private List<CatalogPO> getCatalogsByPeriodUnit(ChronoUnit periodUnit) {
        LambdaQueryWrapper<CatalogPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CatalogPO::getPeriodUnit, periodUnit.name());
        return catalogService.list(queryWrapper);
    }
}
