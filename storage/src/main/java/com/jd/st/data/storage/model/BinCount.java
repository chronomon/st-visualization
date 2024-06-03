package com.jd.st.data.storage.model;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.jd.st.data.storage.hbase.HBaseAdaptor;
import com.jd.st.data.storage.index.ByteArrays;
import com.jd.st.data.storage.index.Z2Range;
import com.jd.st.data.storage.tile.TileCoord;
import org.apache.commons.math3.util.Pair;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * <Bin> : List<Tile, Count>
 */
public class BinCount {
    public final long binEpochSeconds;
    private Map<Long, Long> z2CountMap = null;

    public BinCount(long binEpochSeconds) {
        this.binEpochSeconds = binEpochSeconds;
    }

    public void setZ2Count(Map<Long, Long> z2CountMap) {
        this.z2CountMap = z2CountMap;
    }

    public Map<Long, Long> getZ2CountMap(){
        return z2CountMap;
    }

    public Map<Long, Long> filter(TileCoord tileCoord) {
        Z2Range z2Range = tileCoord.getDataZ2Range(HBaseAdaptor.pyramid.z2Index.maxZoomLevel);
        Map<Long, Long> subZ2CountMap = new HashMap<>();
        for (Map.Entry<Long, Long> z2AndCount : z2CountMap.entrySet()) {
            if (z2Range.contains(z2AndCount.getKey())) {
                subZ2CountMap.put(z2AndCount.getKey(), z2AndCount.getValue());
            }
        }
        return subZ2CountMap;
    }

    public Pair<Long, Long> tileAndFeatureCount(int zoomLevel) {
        long step = HBaseAdaptor.pyramid.z2Index.maxZoomLevel - zoomLevel;
        long featureCount = z2CountMap.values().stream().reduce(Long::sum).orElse(0L);
        long tileCount = z2CountMap.keySet().stream().map(z2 -> z2 >> (2 * step)).distinct().count();
        return new Pair<>(tileCount, featureCount);
    }

    public KVPair encodeKV() {
        return new KVPair(encodeKey(this.binEpochSeconds), encodeValue());
    }

    public static byte[] encodeKey(long binEpochSeconds) {
        byte[] keyBytes = new byte[8];
        ByteArrays.writeLong(binEpochSeconds, keyBytes, 0);
        return keyBytes;
    }

    public static long decodeKey(byte[] bytes) {
        return ByteArrays.readLong(bytes, 0);
    }

    private byte[] encodeValue() {
        if (z2CountMap == null || z2CountMap.isEmpty()) {
            return new byte[0];
        }

        // z2分组
        Map<Long, Map<Short, Long>> group = new HashMap<>();
        for (Map.Entry<Long, Long> subZ2AndCount : z2CountMap.entrySet()) {
            long minSubZ2 = (subZ2AndCount.getKey() >> 14) << 14;
            short relativeSubZ2 = (short) (subZ2AndCount.getKey() - minSubZ2);
            if (group.containsKey(minSubZ2)) {
                group.get(minSubZ2).put(relativeSubZ2, subZ2AndCount.getValue());
            } else {
                Map<Short, Long> subMap = new HashMap<>();
                subMap.put(relativeSubZ2, subZ2AndCount.getValue());
                group.put(minSubZ2, subMap);
            }
        }

        // 每组进行序列化
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Output output = new Output(bos);
        output.writeInt(group.size());
        for (Map.Entry<Long, Map<Short, Long>> minZ2AndSubMap : group.entrySet()) {
            output.writeLong(minZ2AndSubMap.getKey());
            output.writeInt(minZ2AndSubMap.getValue().size());
            for (Map.Entry<Short, Long> relativeZ2AndCount : minZ2AndSubMap.getValue().entrySet()) {
                output.writeShort(relativeZ2AndCount.getKey());
                output.writeVarInt( relativeZ2AndCount.getValue().intValue(), true);
            }
        }
        output.flush();
        return bos.toByteArray();
    }

    public static Map<Long, Long> decodeValue( byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        //deserialize
        Input input = new Input(bytes);
        int groupNum = input.readInt();
        Map<Long, Long> subZ2CountMap = new HashMap<>();
        while (groupNum-- > 0) {
            long minSubZ2 = input.readLong();
            int countNum = input.readInt();
            while (countNum-- > 0) {
                long subZ2 = minSubZ2 + input.readShort();
                long count = input.readVarInt(true);
                subZ2CountMap.put(subZ2, count);
            }
        }

        return subZ2CountMap;
    }
}
