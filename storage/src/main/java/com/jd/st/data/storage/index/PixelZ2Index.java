package com.jd.st.data.storage.index;

import com.jd.st.data.storage.tile.MercatorCRS;

import java.io.Serializable;

public class PixelZ2Index implements Serializable {

    public final int maxZoomLevel;

    public final int tileExtent;

    public final BitNormalization lon;

    public final BitNormalization lat;

    private final double resolution;

    public PixelZ2Index(int maxZoomLevel, int tileExtent) {
        this.maxZoomLevel = maxZoomLevel;
        this.tileExtent = tileExtent;
        this.resolution = MercatorCRS.EXTENT / ((1 << this.maxZoomLevel) * this.tileExtent);
        this.lon = new BitNormalization(0, tileExtent * (1L << this.maxZoomLevel), this.maxZoomLevel);
        this.lat = this.lon;
    }

    public double toPixelX(double projectX) {
        return Math.round((projectX - MercatorCRS.MIN_VALUE) / resolution);
    }

    public double toPixelY(double projectY) {
        return Math.round((MercatorCRS.MAX_VALUE - projectY) / resolution);
    }

    public static long encodeTile(int tileX, int tileY) {
        return split(tileX) | split(tileY) << 1;
    }

    public static int[] decodeTile(long z2) {
        int[] xy = new int[2];
        xy[0] = combine(z2);
        xy[1] = combine(z2 >> 1);
        return xy;
    }

    private static final long MaxMask = 0x7fffffffL;

    private static long split(long value) {
        long x = value & MaxMask;
        x = (x ^ (x << 32)) & 0x00000000ffffffffL;
        x = (x ^ (x << 16)) & 0x0000ffff0000ffffL;
        x = (x ^ (x << 8)) & 0x00ff00ff00ff00ffL; // 11111111000000001111111100000000..
        x = (x ^ (x << 4)) & 0x0f0f0f0f0f0f0f0fL; // 1111000011110000
        x = (x ^ (x << 2)) & 0x3333333333333333L; // 11001100..
        x = (x ^ (x << 1)) & 0x5555555555555555L; // 1010...
        return x;
    }

    private static int combine(long value) {
        long x = value & 0x5555555555555555L;
        x = (x ^ (x >> 1)) & 0x3333333333333333L;
        x = (x ^ (x >> 2)) & 0x0f0f0f0f0f0f0f0fL;
        x = (x ^ (x >> 4)) & 0x00ff00ff00ff00ffL;
        x = (x ^ (x >> 8)) & 0x0000ffff0000ffffL;
        x = (x ^ (x >> 16)) & 0x00000000ffffffffL;
        return (int) x;
    }
}
