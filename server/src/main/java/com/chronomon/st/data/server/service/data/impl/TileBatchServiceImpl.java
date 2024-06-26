package com.chronomon.st.data.server.service.data.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chronomon.st.data.model.collection.PeriodTileCollection;
import com.chronomon.st.data.model.feature.MapFeature;
import com.chronomon.st.data.server.dao.TileBatchMapper;
import com.chronomon.st.data.server.model.param.TileTemporalQueryParam;
import com.chronomon.st.data.server.model.entity.TileBatchPO;
import com.chronomon.st.data.server.service.data.ITileBatchService;
import com.chronomon.st.data.model.statistic.TileStatistic;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TileBatchServiceImpl extends ServiceImpl<TileBatchMapper, TileBatchPO> implements ITileBatchService {
    // 模板表名
    private static final String TEMPLATE_TABLE = "t_template_gps_batch";

    // 用户表名前缀
    private static final String USER_TABLE_PREFIX = "t_user_gps_tile_batch_";

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
    public boolean saveFeatures(List<PeriodTileCollection> tileCollectionList) {
        List<TileBatchPO> tileBatchPOList = new ArrayList<>(tileCollectionList.size());
        for (PeriodTileCollection tileCollection : tileCollectionList) {
            String combineIndex = tileCollection.tileLocation.getZVal() + "_" + tileCollection.periodStartTime.getEpochSecond();
            byte[] featureBytes = tileCollection.serializeFeatures();

            TileBatchPO tileBatchPO = new TileBatchPO();
            tileBatchPO.setCombineIndex(combineIndex);
            tileBatchPO.setDataBatch(featureBytes);

            tileBatchPOList.add(tileBatchPO);
        }

        return saveBatch(tileBatchPOList);
    }

    @Override
    public List<MapFeature> getFeatures(TileTemporalQueryParam param, Map<Instant, TileStatistic> period2TileStatistic) {
        // 构造索引值
        List<String> indexList = new ArrayList<>();
        period2TileStatistic.forEach((key, value) -> {
            final long periodSeconds = key.getEpochSecond();
            value.getZValList().forEach(zVal -> {
                String index = zVal + "_" + periodSeconds;
                indexList.add(index);
            });
        });
        if (indexList.isEmpty()) {
            return Collections.emptyList();
        }

        // 时空数据查询 + 坐标转换
        LambdaQueryWrapper<TileBatchPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(TileBatchPO::getCombineIndex, indexList);
        return list(queryWrapper).stream()
                .map(po -> po.toCollection(param.getTileExtent()))
                .flatMap(tileCollection -> tileCollection.getFeatureList().stream())
                .collect(Collectors.toList());
    }
}
