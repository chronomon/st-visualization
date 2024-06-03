package com.jd.st.data.storage.model;

import com.jd.st.data.storage.hbase.HBaseAdaptor;
import com.jd.st.data.storage.index.PixelZ2Index;
import com.jd.st.data.storage.index.Z2Range;
import com.jd.st.data.storage.tile.MercatorCRS;
import com.jd.st.data.storage.tile.TileCoord;
import org.apache.commons.math3.util.Pair;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class BinTilePyramid implements Serializable {

    public final Period period;

    public final PixelZ2Index z2Index;

    public BinTilePyramid(Period period, int maxZoomLevel, int tileExtent) {
        this.period = period;
        this.z2Index = new PixelZ2Index(maxZoomLevel, tileExtent);
    }

    public Pair<BinTileFeature, TileFeature> locateFeature(GeodeticFeature geodeticFeature) {
        // tile and bin locate
        long pixelX = (long) z2Index.toPixelX(MercatorCRS.geodetic2ProjectX(geodeticFeature.geodeticX));
        long pixelY = (long) z2Index.toPixelY(MercatorCRS.geodetic2ProjectY(geodeticFeature.geodeticY));
        int tileX = z2Index.lon.normalize(pixelX);
        int tileY = z2Index.lat.normalize(pixelY);
        long z2 = PixelZ2Index.encodeTile(tileX, tileY);
        long binEpochSeconds = period.binMinEpochSeconds(geodeticFeature.epochSeconds);

        // calculate offset
        short pixelXOffset = (short) (pixelX - tileX * z2Index.tileExtent);
        short pixelYOffset = (short) (pixelY - tileY * z2Index.tileExtent);
        int secondOffset = (int) (geodeticFeature.epochSeconds - binEpochSeconds);

        // build and return
        BinTileFeature binTile = new BinTileFeature(binEpochSeconds, z2);
        TileFeature tileFeature = new TileFeature(geodeticFeature.oid, pixelXOffset, pixelYOffset, secondOffset);
        return new Pair<>(binTile, tileFeature);
    }

    public List<TileFeature.DataFeature> scanDataFeature(TileCoord tileCoord, long minBinEpochSeconds, long maxBinEpochSeconds, boolean isRaw) {
        Z2Range z2Range = tileCoord.getDataZ2Range(z2Index.maxZoomLevel);
        long binDurationSeconds = HBaseAdaptor.pyramid.period.binDurationSeconds();
        long binEpochSeconds = minBinEpochSeconds;
        List<KVPair> startStopRow = new ArrayList<>();
        while (binEpochSeconds <= maxBinEpochSeconds) {
            byte[] startRow = BinTileFeature.encodeKey(new BinTileFeature(binEpochSeconds, z2Range.minZ2));
            byte[] stopRow = BinTileFeature.encodeKey(new BinTileFeature(binEpochSeconds, z2Range.maxZ2 + 1));
            startStopRow.add(new KVPair(startRow, stopRow));
            binEpochSeconds += binDurationSeconds;
        }

        String tableName = isRaw ? HBaseAdaptor.rawTableName : HBaseAdaptor.pixelTableName;
        List<KVPair> kvPairs = HBaseAdaptor.scanData(tableName, startStopRow);
        return recoverRawFeature(tileCoord, kvPairs, isRaw);
    }

    private List<TileFeature.DataFeature> recoverRawFeature(TileCoord tileCoord, List<KVPair> kvList, boolean isRaw) {
        int step = this.z2Index.maxZoomLevel - tileCoord.zoomLevel;
        int tileMinPixelX = this.z2Index.tileExtent * tileCoord.columnNum;
        int tileMinPixelY = this.z2Index.tileExtent * tileCoord.rowNum;

        List<TileFeature.DataFeature> dataFeatureList = new ArrayList<>();
        for (KVPair kv : kvList) {
            Pair<BinTileFeature, TileFeature> pair;
            if (isRaw) {
                GeodeticFeature geodeticFeature = GeodeticFeature.decode(kv);
                pair = locateFeature(geodeticFeature);
            } else {
                pair = TileFeature.decode(kv);
            }
            BinTileFeature binTile = pair.getKey();
            TileFeature tileFeature = pair.getValue();
            // feature relative pixel and time recover to specific tile's pixel and absolute time
            int[] tileXY = PixelZ2Index.decodeTile(binTile.z2);
            long subTilePixelX = (long) this.z2Index.lon.denormalize(tileXY[0]);
            long subTilePixelY = (long) this.z2Index.lat.denormalize(tileXY[1]);
            short subTileDeltaPixelX = (short) ((subTilePixelX >> step) - tileMinPixelX);
            short subTileDeltaPixelY = (short) ((subTilePixelY >> step) - tileMinPixelY);

            long tilePixelX = (tileFeature.pixelXOffset >> step) + subTileDeltaPixelX;
            long tilePixelY = (tileFeature.pixelYOffset >> step) + subTileDeltaPixelY;
            long epochSeconds = binTile.binEpochSeconds + tileFeature.secondOffset;
            dataFeatureList.add(new TileFeature.DataFeature(tileFeature.oid, tilePixelX, tilePixelY, epochSeconds));
        }
        return dataFeatureList;
    }

    public List<TileFeature.DataFeature> fetchDataFeature(TileCoord tileCoord, long minBinEpochSeconds, long maxBinEpochSeconds) {
        // 生成当前瓦片内的统计量
        Map<Long, BinCount> binTileCountMap = getBinMap(minBinEpochSeconds, maxBinEpochSeconds);
        List<KVPair> kvPairs = new ArrayList<>();
        for (BinCount binCount : binTileCountMap.values()) {
            Map<Long, Long> countMap = binCount.filter(tileCoord);
            for (long z2 : countMap.keySet()) {
                byte[] key = BinTileFeature.encodeKey(new BinTileFeature(binCount.binEpochSeconds, z2));
                kvPairs.add(new KVPair(key, null));
            }
        }

        List<TileFeature.DataFeature> tileFeatureList = new ArrayList<>();
        byte[][] keys = kvPairs.stream().map(kv -> kv.key).toArray(byte[][]::new);
        kvPairs = HBaseAdaptor.multiGetData(HBaseAdaptor.dataTableName, keys);
        for (KVPair kv : kvPairs) {
            tileFeatureList.addAll(recoverFeature(tileCoord, kv));
        }
        return tileFeatureList;
    }

    private List<TileFeature.DataFeature> recoverFeature(TileCoord tileCoord, KVPair kv) {
        int step = this.z2Index.maxZoomLevel - tileCoord.zoomLevel;
        int tileMinPixelX = this.z2Index.tileExtent * tileCoord.columnNum;
        int tileMinPixelY = this.z2Index.tileExtent * tileCoord.rowNum;

        BinTileFeature binTile = BinTileFeature.decodeKey(kv.key);
        List<TileFeature> tileFeatures = BinTileFeature.decodeValue(kv.value);

        // feature relative pixel and time recover to specific tile's pixel and absolute time
        int[] tileXY = PixelZ2Index.decodeTile(binTile.z2);
        long subTilePixelX = (long) this.z2Index.lon.denormalize(tileXY[0]);
        long subTilePixelY = (long) this.z2Index.lat.denormalize(tileXY[1]);
        short subTileDeltaPixelX = (short) ((subTilePixelX >> step) - tileMinPixelX);
        short subTileDeltaPixelY = (short) ((subTilePixelY >> step) - tileMinPixelY);

        return tileFeatures.stream().map(tileFeature -> {
            long tilePixelX = (tileFeature.pixelXOffset >> step) + subTileDeltaPixelX;
            long tilePixelY = (tileFeature.pixelYOffset >> step) + subTileDeltaPixelY;
            long epochSeconds = binTile.binEpochSeconds + tileFeature.secondOffset;
            return new TileFeature.DataFeature(tileFeature.oid, tilePixelX, tilePixelY, epochSeconds);
        }).collect(Collectors.toList());
    }

    public List<TileFeature.CountFeature> fetchPatchFeature(TileCoord tileCoord, long minBinEpochSeconds, long maxBinEpochSeconds) {
        // 生成当前瓦片内targetLevel级的统计量
        Map<Long, BinCount> binTileCountMap = getBinMap(minBinEpochSeconds, maxBinEpochSeconds);
        int targetZoomLevel = Math.min(tileCoord.zoomLevel + 8, z2Index.maxZoomLevel);
        int interval = z2Index.maxZoomLevel - targetZoomLevel;
        Map<Long, Long> subZ2CountMap = new HashMap<>();
        for (BinCount binCount : binTileCountMap.values()) {
            Map<Long, Long> countMap = binCount.filter(tileCoord);
            for (Map.Entry<Long, Long> z2AndCount : countMap.entrySet()) {
                long z2 = z2AndCount.getKey() >> (interval * 2);
                subZ2CountMap.merge(z2, z2AndCount.getValue(), Long::sum);
            }
        }

        // 生成targetLevel级的绝对坐标像素块
        List<TileFeature.CountFeature> featureList = new ArrayList<>();
        GeometryFactory factory = new GeometryFactory();
        PixelZ2Index targetZ2Index = new PixelZ2Index(targetZoomLevel, z2Index.tileExtent);
        for (Map.Entry<Long, Long> z2AndCount : subZ2CountMap.entrySet()) {
            int[] tileXY = PixelZ2Index.decodeTile(z2AndCount.getKey());
            double subTilePixelMinX = targetZ2Index.lon.denormalize(tileXY[0]);
            double subTilePixelMaxX = targetZ2Index.lon.denormalize(tileXY[0] + 1);
            double subTilePixelMinY = targetZ2Index.lat.denormalize(tileXY[1]);
            double subTilePixelMaxY = targetZ2Index.lat.denormalize(tileXY[1] + 1);
            Geometry geom = factory.toGeometry(
                    new Envelope(subTilePixelMinX, subTilePixelMaxX, subTilePixelMinY, subTilePixelMaxY));
            featureList.add(new TileFeature.CountFeature(z2AndCount.getValue(), geom));
        }

        // 转tile.zoomLevel级的绝对坐标后再转tile的相对坐标
        int step = targetZoomLevel - tileCoord.zoomLevel;
        int tileMinPixelX = this.z2Index.tileExtent * tileCoord.columnNum;
        int tileMinPixelY = this.z2Index.tileExtent * tileCoord.rowNum;
        featureList.forEach(countFeature -> {
            Arrays.stream(countFeature.geom.getCoordinates()).forEach(coordinate -> {
                long tilePixelX = ((long) coordinate.getX() >> step) - tileMinPixelX;
                long tilePixelY = ((long) coordinate.getY() >> step) - tileMinPixelY;
                coordinate.setX(tilePixelX);
                coordinate.setY(tilePixelY);
            });
            countFeature.geom.geometryChanged();
        });

        return featureList;
    }

    public Map<Long, Long> getZ2CountMap(long minBinEpochSeconds, long maxBinEpochSeconds) {
        Map<Long, BinCount> binTileCountMap = getBinMap(minBinEpochSeconds, maxBinEpochSeconds);
        Map<Long, Long> z2CountMap = new HashMap<>();
        for (BinCount binCount : binTileCountMap.values()) {
            for (Map.Entry<Long, Long> z2Count : binCount.getZ2CountMap().entrySet()) {
                z2CountMap.merge(z2Count.getKey(), z2Count.getValue(), Long::sum);
            }
        }
        return z2CountMap;
    }

    public long getPacketNum(long minBinEpochSeconds, long maxBinEpochSeconds) {
        Map<Long, BinCount> binTileCountMap = getBinMap(minBinEpochSeconds, maxBinEpochSeconds);
        return binTileCountMap.values().stream()
                .map(s -> s.getZ2CountMap().size())
                .reduce(Integer::sum).orElse(0);
    }

    /**
     * 当前瓦片中的数据总量
     *
     * @param tileCoord          当前瓦片坐标
     * @param minBinEpochSeconds 最小时间
     * @param maxBinEpochSeconds 最大时间
     * @return 数据总量
     */
    public long countTile(TileCoord tileCoord, long minBinEpochSeconds, long maxBinEpochSeconds) {
        // 当前瓦片内的数量
        Map<Long, BinCount> binTileCountMap = getBinMap(minBinEpochSeconds, maxBinEpochSeconds);
        return binTileCountMap.values().stream()
                .flatMap(binCount -> binCount.filter(tileCoord).values().stream())
                .reduce(Long::sum).orElse(0L);
    }

    public  Map<Long, BinCount> getBinMap(long minBinEpochSeconds, long maxBinEpochSeconds) {
        long binDurationSeconds = HBaseAdaptor.pyramid.period.binDurationSeconds();
        long binEpochSeconds = minBinEpochSeconds;

        Map<Long, BinCount> binTileCountMap = new HashMap<>();
        List<Long> missedBinList = new ArrayList<>();
        while (binEpochSeconds <= maxBinEpochSeconds) {
            BinCount binCount = BinCountHolder.getBinCount(binEpochSeconds);
            if (binCount == null) {
                // 如果缓存中没有
                missedBinList.add(binEpochSeconds);
            } else {
                binTileCountMap.put(binEpochSeconds, binCount);
            }
            binEpochSeconds += binDurationSeconds;
        }

        // 获取缓存中没有的统计量
        byte[][] keys = new byte[missedBinList.size()][];
        for (int i = 0; i < missedBinList.size(); i++) {
            keys[i] = BinCount.encodeKey(missedBinList.get(i));
        }
        List<KVPair> kvPairs = HBaseAdaptor.multiGetData(HBaseAdaptor.countTableName, keys);
        for (KVPair kv : kvPairs) {
            long missedBinEpochSeconds = BinCount.decodeKey(kv.key);
            Map<Long, Long> z2CountMap = BinCount.decodeValue(kv.value);
            BinCount missedBinCount = new BinCount(missedBinEpochSeconds);
            missedBinCount.setZ2Count(z2CountMap);
            BinCountHolder.putBinCount(missedBinCount);
            binTileCountMap.put(missedBinEpochSeconds, missedBinCount);
        }

        return binTileCountMap;
    }
}
