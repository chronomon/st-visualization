package com.jd.st.data.storage.collection;

import com.jd.st.data.storage.feature.MapFeature;
import com.jd.st.data.storage.serialize.MapFeaturesSerializer;

import java.util.List;

/**
 * 同一个OID在一个时间片内的时空对象集合
 * 每个时空对象用地图坐标表示
 *
 * @author wangrubin
 */
public class FeatureCollectionOfOid extends AbstractFeatureCollection<FeatureCollectionOfOid, MapFeature> {

    public final String oid;

    public FeatureCollectionOfOid(String oid, List<MapFeature> mapFeatureList) {
        super(mapFeatureList, new MapFeaturesSerializer(oid));
        this.oid = oid;
    }

    public FeatureCollectionOfOid(String oid, byte[] mapFeatureBytes) {
        super(mapFeatureBytes, new MapFeaturesSerializer(oid));
        this.oid = oid;
    }

    @Override
    protected FeatureCollectionOfOid createSubCollection(List<MapFeature> features) {
        return new FeatureCollectionOfOid(oid, features);
    }
}
