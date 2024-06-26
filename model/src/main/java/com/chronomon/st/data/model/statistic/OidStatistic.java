package com.chronomon.st.data.model.statistic;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class OidStatistic {

    private Map<String, Integer> oid2Count;

    public OidStatistic(Map<String, Integer> oid2Count) {
        this.oid2Count = oid2Count;
    }

    public OidStatistic(byte[] statisticBytes) {
        deserialize(statisticBytes);
    }

    public byte[] serialize() {
        if (oid2Count == null || oid2Count.isEmpty()) {
            return new byte[0];
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (Output output = new Output(bos)) {
            // 记录总数
            output.writeVarInt(oid2Count.size(), true);
            for (Map.Entry<String, Integer> entry : oid2Count.entrySet()) {
                // 记录每个OID对应的数据量
                output.writeString(entry.getKey());
                output.writeVarInt(entry.getValue(), true);
            }
            output.flush();
        }

        return bos.toByteArray();
    }

    private void deserialize(byte[] statisticBytes) {
        this.oid2Count = new HashMap<>();
        if (statisticBytes == null || statisticBytes.length == 0) {
            return;
        }

        try (Input input = new Input(statisticBytes)) {
            // 获取总数
            int size = input.readVarInt(true);
            while (size-- > 0) {
                // 获取每个OID对应的数据量
                oid2Count.put(input.readString(), input.readVarInt(true));
            }
        }
    }
}
