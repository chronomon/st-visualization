package com.jd.st.data.server.service.impl;

import com.jd.st.data.server.common.TileEncoder;
import com.jd.st.data.server.service.ITileService;
import com.jd.st.data.storage.hbase.HBaseAdaptor;
import com.jd.st.data.storage.model.TileFeature;
import com.jd.st.data.storage.tile.TileCoord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Slf4j
@Service
public class TileServiceImpl implements ITileService {
    private static final long minBinEpochSeconds = HBaseAdaptor.pyramid.period.binMinEpochSeconds(
            //Timestamp.valueOf("2014-03-29 08:00:00").toInstant().getEpochSecond());
            Timestamp.valueOf("2018-10-10 08:00:00").toInstant().getEpochSecond());
    private static final long maxBinEpochSeconds = HBaseAdaptor.pyramid.period.binMinEpochSeconds(
            //Timestamp.valueOf("2014-03-30 20:00:00").toInstant().getEpochSecond());
            Timestamp.valueOf("2018-10-10 09:00:00").toInstant().getEpochSecond());

    @Override
    public byte[] getTile(TileCoord tileCoord) {
        if (maxBinEpochSeconds == minBinEpochSeconds && tileCoord.zoomLevel == HBaseAdaptor.pyramid.z2Index.maxZoomLevel) {
            // 对应一个时空瓦片中的数据时，不管数量多大都要显示真实数据，保证真实数据的可见性
            return dataTile(tileCoord);
        }

        long start = System.currentTimeMillis();
        //long countPerTile = HBaseAdaptor.pyramid.avgCountPerTile((byte) tileCoord.zoomLevel, minBinEpochSeconds, maxBinEpochSeconds);
        long countCurrTile = HBaseAdaptor.pyramid.countTile(tileCoord, minBinEpochSeconds, maxBinEpochSeconds);
        System.out.println("获取统计量耗时：" + (System.currentTimeMillis() - start));

        if (countCurrTile < 20000) {
            // 数据量足够小，显示真实数据
            return dataTile(tileCoord);
        } else {
            // 数据量太大，显示真实数据
            return patchTile(tileCoord);
        }
    }

    private byte[] patchTile(TileCoord tileCoord) {
        long start = System.currentTimeMillis();
        List<TileFeature.CountFeature> features = HBaseAdaptor.pyramid.fetchPatchFeature(tileCoord, minBinEpochSeconds, maxBinEpochSeconds);
        System.out.println("获取热力数据耗时：" + (System.currentTimeMillis() - start));
        return TileEncoder.generatePatchTile(features);
    }

    private byte[] dataTile(TileCoord tileCoord) {
        long start = System.currentTimeMillis();
        //List<TileFeature.DataFeature> features = HBaseAdaptor.pyramid.fetchDataFeature(tileCoord, minBinEpochSeconds, maxBinEpochSeconds);
        List<TileFeature.DataFeature> features = HBaseAdaptor.pyramid.scanDataFeature(tileCoord, minBinEpochSeconds, maxBinEpochSeconds, false);
        System.out.println("获取位置数据耗时：" + (System.currentTimeMillis() - start));
        return TileEncoder.generateDataTile(features);
    }
}
