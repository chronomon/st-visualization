package com.jd.st.data.storage.index;

import java.io.Serializable;

public class BitNormalization implements Serializable {

    private final double min;
    private final double max;
    private final long bins;
    private final double normalizer;
    private final double denormalizer;

    public BitNormalization(double min, double max, int precision) {
        assert precision > 0 && precision < 32 : "Precision (bits) must be in [1,31]";
        this.min = min;
        this.max = max;
        this.bins = 1L << precision;
        this.normalizer = bins / (max - min);
        this.denormalizer = (max - min) / bins;
    }

    private int maxIndex() {
        return (int) (bins - 1);
    }

    public int normalize(double x) {
        if (x >= max) {
            return maxIndex();
        } else {
            return (int) Math.floor((x - min) * normalizer);
        }
    }

    public double denormalize(int x) {
        if (x >= maxIndex()) {
            return min + maxIndex() * denormalizer;
        } else {
            return min + x * denormalizer;
        }
    }
}