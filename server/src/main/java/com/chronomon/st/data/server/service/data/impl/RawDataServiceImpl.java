package com.chronomon.st.data.server.service.data.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chronomon.st.data.model.feature.MapFeature;
import com.chronomon.st.data.model.pyramid.MapDescriptor;
import com.chronomon.st.data.model.pyramid.TileMapLocation;
import com.chronomon.st.data.model.statistic.TileStatistic;
import com.chronomon.st.data.server.catalog.CatalogContext;
import com.chronomon.st.data.server.dao.RawDataMapper;
import com.chronomon.st.data.server.model.entity.CatalogPO;
import com.chronomon.st.data.server.model.entity.RawDataPO;
import com.chronomon.st.data.server.service.data.IRawDataService;
import com.chronomon.st.data.server.service.task.DataRollScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RawDataServiceImpl extends ServiceImpl<RawDataMapper, RawDataPO> implements IRawDataService {

    // 模板表名
    private static final String TEMPLATE_TABLE = "t_template_gps_raw";

    // 用户表名前缀
    private static final String USER_TABLE_PREFIX = "t_user_gps_raw_";

    @Resource
    private DataRollScheduler dataRollScheduler;

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
    public boolean saveFeatures(List<MapFeature> features) {
        CatalogPO catalogPO = CatalogContext.getCatalog();
        MapDescriptor mapDescriptor = new MapDescriptor(catalogPO.getMaxZoomLevel(), catalogPO.getTileExtent());
        long maxFeatureTime = 0L;
        List<RawDataPO> rawDataPOList = new ArrayList<>(features.size());
        for (MapFeature mapFeature : features) {
            TileMapLocation tileMapLocation = mapDescriptor.locate2Tile(mapFeature);
            RawDataPO rawDataPO = new RawDataPO();
            rawDataPO.setOid(mapFeature.oid);
            rawDataPO.setTime(mapFeature.time.getEpochSecond());
            rawDataPO.setMapX(mapFeature.mapX);
            rawDataPO.setMapY(mapFeature.mapY);
            rawDataPO.setZVal(tileMapLocation.getZVal());

            maxFeatureTime = Math.max(maxFeatureTime, mapFeature.time.getEpochSecond());
            rawDataPOList.add(rawDataPO);
        }

        ChronoUnit periodUnit = ChronoUnit.valueOf(catalogPO.getPeriodUnit());
        long currentPeriod = Instant.ofEpochSecond(maxFeatureTime).truncatedTo(periodUnit).getEpochSecond();
        if (currentPeriod > catalogPO.getNextRollPeriod()) {
            // 新插入的位置数据位于一个全新的时间片，触发对上一个时间片内数据的归档操作
            dataRollScheduler.registerTrigger(catalogPO.getCatalogId(), currentPeriod);
        }
        return saveBatch(rawDataPOList);
    }

    @Override
    public TileStatistic getTileStatistic(long minZVal, long maxZVal) {
        String catalogId = CatalogContext.getCatalog().getCatalogId();
        String tableName = USER_TABLE_PREFIX + catalogId;
        Map<Long, Integer> zVal2Count = getBaseMapper().statistic(tableName, minZVal, maxZVal).stream()
                .collect(Collectors.toMap(RawDataMapper.ZValAndCount::getZVal, RawDataMapper.ZValAndCount::getCount));
        return new TileStatistic(zVal2Count);
    }

    @Override
    public List<MapFeature> getFeaturesByOid(String oid) {
        // 基于OID的对象查询
        LambdaQueryWrapper<RawDataPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RawDataPO::getOid, oid);
        queryWrapper.orderByAsc(RawDataPO::getTime);

        return list(queryWrapper).stream()
                .map(RawDataPO::toMapFeature)
                .collect(Collectors.toList());
    }

    @Override
    public List<MapFeature> getFeaturesByZRange(long minZVal, long maxZVal) {
        // 基于zVal的空间范围查询
        LambdaQueryWrapper<RawDataPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(RawDataPO::getZVal, minZVal, maxZVal);

        return list(queryWrapper).stream()
                .map(RawDataPO::toMapFeature)
                .collect(Collectors.toList());
    }

    @Override
    public List<MapFeature> getFeaturesByUntilTime(long untilTime) {
        LambdaQueryWrapper<RawDataPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.lt(RawDataPO::getTime, untilTime); // 严格的小于，而非小于等于
        return list(queryWrapper).stream()
                .map(RawDataPO::toMapFeature)
                .collect(Collectors.toList());
    }

    @Override
    public boolean removeByTime(long untilTime) {
        LambdaQueryWrapper<RawDataPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.lt(RawDataPO::getTime, untilTime); // 严格的小于，而非小于等于
        return remove(queryWrapper);
    }
}