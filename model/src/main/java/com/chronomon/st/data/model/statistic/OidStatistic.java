package com.chronomon.st.data.model.statistic;

import java.util.Map;

public class OidStatistic {

    private Map<String, Integer> oid2Count;

    public OidStatistic(Map<String, Integer> oid2Count) {
        this.oid2Count = oid2Count;
    }

    public OidStatistic(byte[] statisticBytes) {
        deserialize(statisticBytes);
    }

    private byte[] serialize() {
        // todo: 序列化
        return null;
    }

    private void deserialize(byte[] statisticBytes) {

        // todo: 反序列化
        this.oid2Count = null;
    }
}
