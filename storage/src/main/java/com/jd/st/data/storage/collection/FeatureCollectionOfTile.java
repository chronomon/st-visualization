package com.jd.st.data.storage.collection;

import com.jd.st.data.storage.feature.TileFeature;
import com.jd.st.data.storage.pyramid.TileLocation;
import com.jd.st.data.storage.serialize.TileFeatureSerializer;

import java.util.List;

/**
 * 同一个瓦片在一个时间片内的时空对象集合
 * 每个时空对象用瓦片坐标表示
 *
 * @author wangrubin
 */
public class FeatureCollectionOfTile extends AbstractFeatureCollection<FeatureCollectionOfTile, TileFeature> {

    public final TileLocation tileLocation;

    public FeatureCollectionOfTile(TileLocation tileLocation, List<TileFeature> tileFeatureList) {
        super(tileFeatureList, new TileFeatureSerializer());
        this.tileLocation = tileLocation;
    }

    public FeatureCollectionOfTile(TileLocation tileLocation, byte[] tileFeatureBytes) {
        super(tileFeatureBytes, new TileFeatureSerializer());
        this.tileLocation = tileLocation;
    }

    @Override
    protected FeatureCollectionOfTile createSubCollection(List<TileFeature> features) {
        return new FeatureCollectionOfTile(tileLocation, features);
    }
}
