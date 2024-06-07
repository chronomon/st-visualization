package com.chronomon.st.data.model.collection;

/**
 * 可序列化的时空对象集合接口，用于实现集合的序列化和反序列化
 *
 * @author wangrubin
 */
public interface SerializableCollection {

    byte[] serializeFeatures();

    void deserializeFeatures(byte[] featureBytes);
}
