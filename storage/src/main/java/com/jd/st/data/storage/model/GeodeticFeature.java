package com.jd.st.data.storage.model;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.jd.st.data.storage.index.ByteArrays;

import java.io.IOException;

public class GeodeticFeature {
    public final String oid;

    public final double geodeticX;

    public final double geodeticY;

    public final long epochSeconds;

    public GeodeticFeature(String oid, double geodeticX, double geodeticY, long epochSeconds) {
        this.oid = oid;
        this.geodeticX = geodeticX;
        this.geodeticY = geodeticY;
        this.epochSeconds = epochSeconds;
    }

    public KVPair encode(BinTileFeature binTileFeature) {

        // 序列化Key
        Output output = new Output(24 + oid.length());
        output.writeLong(binTileFeature.binEpochSeconds);
        output.writeLong(binTileFeature.z2);
        output.writeLong(epochSeconds);
        output.writeBytes(oid.getBytes());
        byte[] keyBytes = output.toBytes();

        // 序列化Value
        output.clear();
        output = new Output(16);
        output.writeDouble(geodeticX);
        output.writeDouble(geodeticY);
        byte[] valueBytes = output.toBytes();

        return new KVPair(keyBytes, valueBytes);
    }

    public static GeodeticFeature decode(KVPair kvPair) {
        Input input = new Input(kvPair.key);

        // 反序列化Key
        input.readLong();
        input.readLong();
        long epochSeconds = input.readLong();
        String oid = "";
        try {
            oid = new String(input.readBytes(input.available()));
        } catch (Exception e) {
            System.out.println("解析OID失败");
        }

        // 反序列化Value
        input = new Input(kvPair.value);
        double geodeticX = input.readDouble();
        double geodeticY = input.readDouble();

        return new GeodeticFeature(oid, geodeticX, geodeticY, epochSeconds);
    }

}
