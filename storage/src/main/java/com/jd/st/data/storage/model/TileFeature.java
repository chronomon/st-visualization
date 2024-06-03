package com.jd.st.data.storage.model;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.apache.commons.math3.util.Pair;
import org.locationtech.jts.geom.Geometry;

import java.io.IOException;
import java.io.Serializable;

public class TileFeature implements Serializable {
    public String oid;
    public short pixelXOffset;
    public short pixelYOffset;
    public int secondOffset;

    private TileFeature() {
    }

    public TileFeature(String oid, short pixelXOffset, short pixelYOffset, int secondOffset) {
        this.oid = oid;
        this.pixelXOffset = pixelXOffset;
        this.pixelYOffset = pixelYOffset;
        this.secondOffset = secondOffset;
    }

    public KVPair encode(BinTileFeature binTileFeature) {
        Output output = new Output(24 + oid.length());

        // 序列化Key
        output.writeLong(binTileFeature.binEpochSeconds);
        output.writeLong(binTileFeature.z2);
        output.writeVarInt(secondOffset, true);
        output.writeBytes(oid.getBytes());
        byte[] keyBytes = output.toBytes();

        // 序列化Value
        output.clear();
        output.writeShort(pixelXOffset);
        output.writeShort(pixelYOffset);
        byte[] valueBytes = output.toBytes();

        return new KVPair(keyBytes, valueBytes);
    }

    public static Pair<BinTileFeature, TileFeature> decode(KVPair kvPair) {
        Input input = new Input(kvPair.key);

        // 反序列化Key
        long binMinEpochSeconds = input.readLong();
        long z2 = input.readLong();
        int secondOffset = input.readVarInt(true);
        String oid = "";
        try {
            oid = new String(input.readBytes(input.available()));
        } catch (Exception e) {
            System.out.println("解析OID失败");
        }

        // 反序列化Value
        input = new Input(kvPair.value);
        short pixelXOffset = input.readShort();
        short pixelYOffset = input.readShort();

        return new Pair<>(new BinTileFeature(binMinEpochSeconds, z2), new TileFeature(oid, pixelXOffset, pixelYOffset, secondOffset));
    }

    public static class DataFeature {
        public final String oid;
        public final long pixelX;
        public final long pixelY;
        public final long epochSeconds;

        public DataFeature(String oid, long pixelX, long pixelY, long epochSeconds) {
            this.oid = oid;
            this.pixelX = pixelX;
            this.pixelY = pixelY;
            this.epochSeconds = epochSeconds;
        }
    }

    public static class CountFeature {
        public final long count;
        public final Geometry geom;

        public CountFeature(long count, Geometry geom) {
            this.count = count;
            this.geom = geom;
        }
    }
}
