package com.jd.st.data.server;

import com.jd.st.data.server.common.TileEncoder;
import com.jd.st.data.storage.hbase.HBaseAdaptor;
import com.jd.st.data.storage.index.PixelZ2Index;
import com.jd.st.data.storage.model.BinCount;
import com.jd.st.data.storage.model.BinTilePyramid;
import com.jd.st.data.storage.model.Period;
import com.jd.st.data.storage.model.TileFeature;
import com.jd.st.data.storage.tile.TileCoord;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class ExperimentTest {

    @Test
    public void generateProjectAndPixel() {
        long minBinEpochSeconds = HBaseAdaptor.pyramid.period.binMinEpochSeconds(
                Timestamp.valueOf("2018-10-01 00:00:00").toInstant().getEpochSecond());
        long maxBinEpochSeconds = HBaseAdaptor.pyramid.period.binMinEpochSeconds(
                Timestamp.valueOf("2018-10-16 00:00:00").toInstant().getEpochSecond());

        int round = 1;
        long threshold = 5000;
        while (round <= 5) {
            Map<Long, Long> z2CountMap = HBaseAdaptor.pyramid.getZ2CountMap(minBinEpochSeconds, maxBinEpochSeconds);
            List<Map.Entry<Long, Long>> z2List = topK(z2CountMap, 20, threshold);
            System.out.println(threshold + "平均每张瓦片耗时：" + statistic(z2List, minBinEpochSeconds, maxBinEpochSeconds));
            threshold += 5000;
            round += 1;
        }
    }

    @Test
    public void countPacket() {
        long minBinEpochSeconds = HBaseAdaptor.pyramid.period.binMinEpochSeconds(
                Timestamp.valueOf("2018-10-01 08:00:00").toInstant().getEpochSecond());
        long maxBinEpochSeconds = HBaseAdaptor.pyramid.period.binMinEpochSeconds(
                Timestamp.valueOf("2018-10-01 20:00:00").toInstant().getEpochSecond());

        for (int zoomLevel : Arrays.asList(15, 16, 17, 18, 19)) {
            HBaseAdaptor.countTableName = String.format("experiment_%s_512_1_count", zoomLevel);
            HBaseAdaptor.pyramid = new BinTilePyramid(new Period(1), zoomLevel, 512);
            long packetNum = HBaseAdaptor.pyramid.getPacketNum(minBinEpochSeconds, maxBinEpochSeconds);
            System.out.println(zoomLevel + "zoomLevel数据包总数：" + packetNum);
        }

        for (int period : Arrays.asList(1, 2, 3, 4, 5)) {
            HBaseAdaptor.countTableName = String.format("experiment_18_512_%s_count", period);
            HBaseAdaptor.pyramid = new BinTilePyramid(new Period(period), 18, 512);
            long packetNum = HBaseAdaptor.pyramid.getPacketNum(minBinEpochSeconds, maxBinEpochSeconds);
            System.out.println(period + "period数据包总数：" + packetNum);
        }
    }

    @Test
    public void countBinFetch() {
        long minBinEpochSeconds = HBaseAdaptor.pyramid.period.binMinEpochSeconds(
                Timestamp.valueOf("2018-10-01 08:00:00").toInstant().getEpochSecond());
        long span = HBaseAdaptor.pyramid.period.binDurationSeconds();

        for (int interval : Arrays.asList(1, 50, 100, 150, 200, 250)) {
            HBaseAdaptor.countTableName = "experiment_18_512_1_count";
            long maxBinEpochSeconds = minBinEpochSeconds + interval * span;
            long start = System.currentTimeMillis();
            HBaseAdaptor.pyramid.getBinMap(minBinEpochSeconds, maxBinEpochSeconds);
            System.out.println(interval + "binNum获取统计量耗时：" + (System.currentTimeMillis() - start));
        }
    }

    @Test
    public void total() {
        long minBinEpochSeconds = HBaseAdaptor.pyramid.period.binMinEpochSeconds(
                Timestamp.valueOf("2018-10-01 00:00:00").toInstant().getEpochSecond());
        long maxBinEpochSeconds = HBaseAdaptor.pyramid.period.binMinEpochSeconds(
                Timestamp.valueOf("2018-12-01 00:00:00").toInstant().getEpochSecond());

        HBaseAdaptor.countTableName = "experiment_18_512_1_count";
        Map<Long, BinCount> map = HBaseAdaptor.pyramid.getBinMap(minBinEpochSeconds, maxBinEpochSeconds);
        long totalCount = map.values().stream().flatMap(entry -> entry.getZ2CountMap().values().stream()).reduce(Long::sum).orElse(0L);
        System.out.println("数据总量：" + totalCount);

    }

    private long statistic(List<Map.Entry<Long, Long>> z2List, long minBinEpochSeconds, long maxBinEpochSeconds) {
        long start = System.currentTimeMillis();
        for (Map.Entry<Long, Long> entry : z2List) {
            int[] tileXY = PixelZ2Index.decodeTile(entry.getKey());
            TileCoord tileCoord = new TileCoord(tileXY[1], tileXY[0], 18);
            byte[] bytes = dataTile(tileCoord, minBinEpochSeconds, maxBinEpochSeconds);
        }
        return (System.currentTimeMillis() - start) / z2List.size();
    }

    private List<Map.Entry<Long, Long>> topK(Map<Long, Long> z2CountMap, int tileCount, long threshold) {
        List<Map.Entry<Long, Long>> sorted = z2CountMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toList());
        List<Map.Entry<Long, Long>> topZ = new ArrayList<>(tileCount);
        for (int i = 0; i <= sorted.size(); i++) {
            if (sorted.get(i).getValue() >= threshold) {
                topZ.add(sorted.get(i));
                if (topZ.size() == tileCount) {
                    break;
                }
            }
        }
        return topZ;
    }

    private byte[] dataTile(TileCoord tileCoord, long minBinEpochSeconds, long maxBinEpochSeconds) {
        long start = System.currentTimeMillis();
        List<TileFeature.DataFeature> features = HBaseAdaptor.pyramid.fetchDataFeature(tileCoord, minBinEpochSeconds, maxBinEpochSeconds);
        //List<TileFeature.DataFeature> features = HBaseAdaptor.pyramid.scanDataFeature(tileCoord, minBinEpochSeconds, maxBinEpochSeconds, false);
        System.out.println("获取位置数据耗时：" + (System.currentTimeMillis() - start));
        return TileEncoder.generateDataTile(features);
    }
}