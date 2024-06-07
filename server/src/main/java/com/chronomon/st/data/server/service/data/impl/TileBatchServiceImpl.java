package com.chronomon.st.data.server.service.data.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chronomon.st.data.server.dao.TileBatchMapper;
import com.chronomon.st.data.server.model.param.TileTemporalQueryParam;
import com.chronomon.st.data.server.utils.TileEncoder;
import com.chronomon.st.data.server.model.entity.TileBatchPO;
import com.chronomon.st.data.server.service.data.ITileBatchService;
import com.chronomon.st.data.model.feature.MapFeature;
import com.chronomon.st.data.model.statistic.TileStatistic;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TileBatchServiceImpl extends ServiceImpl<TileBatchMapper, TileBatchPO> implements ITileBatchService {

    @Override
    public boolean createTable(String catalogName) {
        return getBaseMapper().createTable("t_user_gps_tile_batch" + "_" + catalogName) > 0;
    }

    @Override
    public byte[] dataTile(TileTemporalQueryParam param, Map<Instant, TileStatistic> period2TileStatistic) {
        // 构造索引值
        List<String> indexList = new ArrayList<>();
        period2TileStatistic.forEach((key, value) -> {
            final long periodSeconds = key.getEpochSecond();
            value.getZValList().forEach(zVal -> {
                String index = zVal + "_" + periodSeconds;
                indexList.add(index);
            });
        });

        // 时空数据查询 + 坐标转换
        int zoomLevelStep = param.getZoomLevelStep();
        LambdaQueryWrapper<TileBatchPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(TileBatchPO::getCombineIndex, indexList);
        List<MapFeature> featureList = list(queryWrapper).stream()
                .flatMap(po -> po.toCollection(param.getTileExtent()).getFeatureList().stream())
                .peek(mapFeature -> {
                    mapFeature.mapX = mapFeature.mapX >> zoomLevelStep;
                    mapFeature.mapY = mapFeature.mapY >> zoomLevelStep;
                }).collect(Collectors.toList());

        // 执行矢量瓦片编码
        return TileEncoder.generateDataTile(featureList, param.getTileMapLocation());
    }
}
