package com.chronomon.st.data.server.service.tile.impl;

import com.chronomon.st.data.server.model.param.TileTemporalQueryParam;
import com.chronomon.st.data.server.service.data.ITileBatchService;
import com.chronomon.st.data.server.service.data.ITileStatisticService;
import com.chronomon.st.data.server.service.tile.ITileService;
import com.chronomon.st.data.model.statistic.TileStatistic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
public class TileServiceImpl implements ITileService {

    @Resource
    private ITileBatchService tileBatchService;

    @Resource
    private ITileStatisticService tileStatisticService;

    // todo: 考虑归档和未归档的数据的融合
    @Override
    public byte[] getTile(TileTemporalQueryParam param) {
        Map<Instant, TileStatistic> period2TileStatistic = tileStatisticService.getTileStatistic(param);
        long sum = period2TileStatistic.values().stream().map(TileStatistic::count).reduce(Long::sum).orElse(0L);

        if (param.isMinGrainSize() || sum < 20000) {
            // 数据量较小，显示真实数据
            return tileBatchService.dataTile(param, period2TileStatistic);
        } else {
            // 数据量太大，显示统计数据
            return tileStatisticService.patchTile(param, period2TileStatistic);
        }
    }
}
