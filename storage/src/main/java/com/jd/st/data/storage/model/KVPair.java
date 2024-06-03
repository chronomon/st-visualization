package com.jd.st.data.storage.model;

/**
 * <Key, Value>
 */
public class KVPair {

    public final byte[] key;

    public final byte[] value;

    public KVPair(byte[] key, byte[] value) {
        this.key = key;
        this.value = value;
    }
}
