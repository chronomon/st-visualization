package com.chronomon.st.data.model.collection;

import com.chronomon.st.data.model.feature.MapFeature;
import com.chronomon.st.data.model.pyramid.MapDescriptor;
import com.chronomon.st.data.model.pyramid.TileMapLocation;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 时间片分组后的时空对象集合
 *
 * @author wangrubin
 */
public class PeriodCollection {

    /**
     * 时间片起始时刻
     */
    public final Instant periodStartTime;

    /**
     * 时空对象集合
     */
    private final List<MapFeature> featureList;

    /**
     * 集合不同OID的数量统计
     */
    private Map<String, Integer> oid2Count = null;

    /**
     * 集合不同瓦片的数量统计
     */
    private Map<TileMapLocation, Integer> tile2Count = null;

    public PeriodCollection(Instant periodStartTime, List<MapFeature> featureList) {
        this.periodStartTime = periodStartTime;
        this.featureList = featureList;
        this.featureList.sort(Comparator.comparing(o -> o.time));
    }

    /**
     * 按照瓦片分组
     *
     * @return 时间片 + 瓦片分组后的时空对象集合列表
     */
    public List<PeriodTileCollection> groupByTile(MapDescriptor mapDescriptor) {
        Map<TileMapLocation, List<MapFeature>> tile2FeatureList = new HashMap<>();
        for (MapFeature mapFeature : this.featureList) {
            TileMapLocation tileMapLocation = mapDescriptor.locate2Tile(mapFeature);
            List<MapFeature> featureList = tile2FeatureList.computeIfAbsent(tileMapLocation, key -> new ArrayList<>());
            featureList.add(mapFeature);
        }

        this.tile2Count = tile2FeatureList.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().size()));

        return tile2FeatureList.entrySet().stream()
                .map(entry -> new PeriodTileCollection(this.periodStartTime, entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * 获取当前时间片内，不同oid对应的时空对象数量
     *
     * @return oid的时空对象数量
     */
    public Map<TileMapLocation, Integer> getTile2Count() {
        if (tile2Count == null) {
            throw new RuntimeException("请先调用groupByTile方法");
        }
        return tile2Count;
    }

    /**
     * 按照oid分组
     *
     * @return 时间片 + oid分组后的时空对象集合列表
     */
    public List<PeriodOidCollection> groupByOid() {
        List<PeriodOidCollection> oidCollectionList = this.featureList.stream()
                .collect(Collectors.groupingBy(feature -> feature.oid))
                .entrySet().stream()
                .map(entry -> new PeriodOidCollection(this.periodStartTime, entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        this.oid2Count = oidCollectionList.stream()
                .collect(Collectors.toMap(collection -> collection.oid, PeriodOidCollection::size));

        return oidCollectionList;
    }

    /**
     * 获取当前时间片内，不同oid对应的时空对象数量
     *
     * @return oid的时空对象数量
     */
    public Map<String, Integer> getOid2Count() {
        if (oid2Count == null) {
            throw new RuntimeException("请先调用groupByOid方法");
        }
        return oid2Count;
    }

    /**
     * 将对象集合按照时间片分组
     *
     * @param featureList 对象集合
     * @param periodUnit  时间片单位
     * @return 时间片分组后的对象集合
     */
    public static List<PeriodCollection> groupByPeriod(
            List<MapFeature> featureList, ChronoUnit periodUnit) {

        // 根据时间片分组
        Map<Instant, List<MapFeature>> periodStartTime2Features = new HashMap<>();
        for (MapFeature feature : featureList) {
            Instant startTime = feature.locate2Period(periodUnit);
            List<MapFeature> features = periodStartTime2Features.computeIfAbsent(startTime, key -> new ArrayList<>());
            features.add(feature);
        }

        // 每组创建一个时间片集合对象
        return periodStartTime2Features.entrySet().stream()
                .map(entry -> new PeriodCollection(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }
}
