package com.jd.st.data.storage.model;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.jd.st.data.storage.index.ByteArrays;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <Bin, Tile>:List<Feature>
 */
public class BinTileFeature implements Serializable {
    public final long binEpochSeconds;

    public final long z2;
    private transient List<TileFeature> features = null;

    public BinTileFeature(long binEpochSeconds, long z2) {
        this.binEpochSeconds = binEpochSeconds;
        this.z2 = z2;
    }

    public void setFeatures(List<TileFeature> features) {
        this.features = features;
    }

    public KVPair encodeKV() {
        return new KVPair(encodeKey(this), encodeValue());
    }

    public static byte[] encodeKey(BinTileFeature binTile) {
        byte[] bytes = new byte[16];
        ByteArrays.writeLong(binTile.binEpochSeconds, bytes, 0);
        ByteArrays.writeLong(binTile.z2, bytes, 8);
        return bytes;
    }

    public static BinTileFeature decodeKey(byte[] bytes) {
        long binEpochSeconds = ByteArrays.readLong(bytes, 0);
        long z2 = ByteArrays.readLong(bytes, 8);
        return new BinTileFeature(binEpochSeconds, z2);
    }

    private byte[] encodeValue() {
        if (features == null || features.isEmpty()) {
            return new byte[0];
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Output output = new Output(bos);

        // 序列化oid
        List<String> oidList = features.stream().map(feature -> feature.oid)
                .distinct().collect(Collectors.toList());
        output.writeVarInt(oidList.size(), true);
        for (String oid : oidList) {
            output.writeString(oid);
        }

        // 序列化Feature
        Map<String, Integer> oid2NumMap = new HashMap<>(oidList.size());
        for (int i = 0; i < oidList.size(); i++) {
            oid2NumMap.put(oidList.get(i), i);
        }
        output.writeInt(features.size());
        for (TileFeature feature : features) {
            int oidIndex = oid2NumMap.get(feature.oid);
            output.writeVarInt(oidIndex, true);
            output.writeShort(feature.pixelXOffset);
            output.writeShort(feature.pixelYOffset);
            output.writeVarInt(feature.secondOffset, true);
        }
        output.flush();
        return bos.toByteArray();
    }

    public static List<TileFeature> decodeValue(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        Input input = new Input(bytes);

        // 反序列化oid
        int oidSize = input.readVarInt(true);
        Map<Integer, String> num2OidMap = new HashMap<>(oidSize);
        for (int oidIndex = 0; oidIndex < oidSize; oidIndex++) {
            num2OidMap.put(oidIndex, input.readString());
        }

        // 反序列化Feature
        int featureSize = input.readInt();
        List<TileFeature> features = new ArrayList<>(featureSize);
        while (featureSize-- > 0) {
            int oidIndex = input.readVarInt(true);
            String oid = num2OidMap.get(oidIndex);
            short pixelXOffset = input.readShort();
            short pixelYOffset = input.readShort();
            int secondOffset = input.readVarInt(true);
            features.add(new TileFeature(oid, pixelXOffset, pixelYOffset, secondOffset));
        }

        return features;
    }

    @Override
    public int hashCode() {
        return (byte) Long.hashCode(binEpochSeconds) + (byte) Long.hashCode(z2);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BinTileFeature) {
            BinTileFeature other = (BinTileFeature) obj;
            return this.binEpochSeconds == other.binEpochSeconds &&
                    this.z2 == other.z2;
        }
        return false;
    }
}
