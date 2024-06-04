package com.jd.st.data.storage.collection;

import com.jd.st.data.storage.feature.BaseFeature;
import com.jd.st.data.storage.serialize.IFeaturesSerializer;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 同一个OID在一个时间片内的时空对象集合
 * 每个时空对象用地图坐标表示
 *
 * @author wangrubin
 */
public abstract class AbstractFeatureCollection<C extends AbstractFeatureCollection<C, F>, F extends BaseFeature> {

    protected List<F> featureList;

    protected Instant periodStartTime;

    protected IFeaturesSerializer<F> featuresSerializer;

    public AbstractFeatureCollection(List<F> featureList, IFeaturesSerializer<F> featuresSerializer) {
        // 按照时间递增排序，方便对时间进行压缩
        this.featuresSerializer = featuresSerializer;
        this.featureList = featureList;
        this.featureList.sort(Comparator.comparing(o -> o.time));
        this.periodStartTime = null;
    }

    public AbstractFeatureCollection(byte[] featureBytes, IFeaturesSerializer<F> featuresSerializer) {
        // 按照时间递增排序，方便对时间进行压缩
        this.featuresSerializer = featuresSerializer;
        this.featureList = featuresSerializer.deserializeFeatures(featureBytes);
        this.periodStartTime = null;
    }

    public int size() {
        return featureList.size();
    }

    /**
     * 是否为根据时间片分组后的集合
     *
     * @return true是，false否
     */
    public boolean periodGrouped() {
        return periodStartTime != null;
    }

    public Instant getPeriodStartTime() {
        return periodStartTime;
    }

    public void setPeriodStartTime(Instant periodStartTime) {
        this.periodStartTime = periodStartTime;
    }

    public byte[] featuresAsBytes() {
        return featuresSerializer.serializeFeatures(featureList);
    }

    /**
     * 将时空对象集合根据时间片进行分组
     *
     * @param periodUnit    时间片单位
     * @param usePeriodTime 是否将时空对象的时间替换成时间片内的相对时间
     * @return 根据时间片分片的时空对象集合
     */
    public List<C> groupByPeriod(ChronoUnit periodUnit, boolean usePeriodTime) {
        // 根据时间片分组
        Map<Instant, List<F>> periodStartTime2Features = new HashMap<>();
        for (F feature : this.featureList) {
            Instant startTime = feature.truncate(periodUnit, usePeriodTime);
            List<F> features = periodStartTime2Features.computeIfAbsent(startTime, key -> new ArrayList<>());
            features.add(feature);
        }

        // 每组创建对应的集合对象
        List<C> subCollectionList = new ArrayList<>(periodStartTime2Features.size());
        for (Map.Entry<Instant, List<F>> entry : periodStartTime2Features.entrySet()) {
            C subCollection = createSubCollection(entry.getValue());
            subCollection.periodStartTime = entry.getKey();
            subCollectionList.add(subCollection);
        }

        return subCollectionList;
    }

    /**
     * 根据时间片分区后的子集创建集合对象
     *
     * @param features 时间片内的子集
     * @return 集合对象
     */
    protected abstract C createSubCollection(List<F> features);
}
