package com.jd.st.data.storage.serialize;

import com.jd.st.data.storage.feature.BaseFeature;

import java.util.List;

public interface IFeaturesSerializer<F extends BaseFeature> {

    byte[] serializeFeatures(List<F> features);

    List<F> deserializeFeatures(byte[] bytes);
}
