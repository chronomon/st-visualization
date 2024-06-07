package com.chronomon.st.data.model.statistic;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TileStatistic {

    private Map<Long, Integer> zVal2Count;

    public TileStatistic(Map<Long, Integer> zVal2Count) {
        this.zVal2Count = zVal2Count;
    }

    public TileStatistic(byte[] statisticBytes) {
        deserialize(statisticBytes);
    }

    private byte[] serialize() {
        if (zVal2Count == null || zVal2Count.isEmpty()) {
            return new byte[0];
        }

        // zVal进行分组
        Map<Long, Map<Short, Integer>> group = new HashMap<>();
        for (Map.Entry<Long, Integer> zValAndCount : zVal2Count.entrySet()) {
            long minZVal = (zValAndCount.getKey() >> 14) << 14;
            short relativeZVal = (short) (zValAndCount.getKey() - minZVal);
            group.computeIfAbsent(minZVal, key -> new HashMap<>())
                    .put(relativeZVal, zValAndCount.getValue());
        }

        // 所有分组进行序列化
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (Output output = new Output(bos)) {
            output.writeInt(group.size());
            for (Map.Entry<Long, Map<Short, Integer>> minZ2AndSubMap : group.entrySet()) {
                output.writeLong(minZ2AndSubMap.getKey());
                output.writeInt(minZ2AndSubMap.getValue().size());
                for (Map.Entry<Short, Integer> relativeZValAndCount : minZ2AndSubMap.getValue().entrySet()) {
                    output.writeShort(relativeZValAndCount.getKey());
                    output.writeVarInt(relativeZValAndCount.getValue(), true);
                }
            }
            output.flush();
        }
        return bos.toByteArray();
    }

    private void deserialize(byte[] statisticBytes) {
        this.zVal2Count = new HashMap<>();
        if (statisticBytes == null || statisticBytes.length == 0) {
            return;
        }

        try (Input input = new Input(statisticBytes)) {
            int groupNum = input.readInt();
            while (groupNum-- > 0) {
                long minZVal = input.readLong();
                int countNum = input.readInt();
                while (countNum-- > 0) {
                    long zVal = minZVal + input.readShort();
                    int count = input.readVarInt(true);
                    zVal2Count.put(zVal, count);
                }
            }
        }
    }

    public Map<Long, Integer> getzVal2Count() {
        return zVal2Count;
    }

    public TileStatistic filter(long minZVal, long maxZVal) {
        Map<Long, Integer> subZVal2Count = new HashMap<>();
        for (Map.Entry<Long, Integer> zValAndCount : zVal2Count.entrySet()) {
            if (zValAndCount.getKey() >= minZVal && zValAndCount.getKey() <= maxZVal) {
                subZVal2Count.put(zValAndCount.getKey(), zValAndCount.getValue());
            }
        }
        return new TileStatistic(subZVal2Count);
    }

    public long count() {
        return zVal2Count.values().stream().reduce(Integer::sum).orElse(0);
    }

    public List<Long> getZValList() {
        return new ArrayList<>(zVal2Count.keySet());
    }
}
