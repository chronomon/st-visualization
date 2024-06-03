package com.jd.st.data.storage.model;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;

public class BinCountHolder {

    private static final long MAX_CACHE_COUNT = 500;

    private static final Cache<Long, BinCount> binCountCache =
            Caffeine.newBuilder()
                    .expireAfterAccess(30, TimeUnit.MINUTES)
                    .maximumSize(MAX_CACHE_COUNT).build();

    public static BinCount getBinCount(long binEpochSeconds) {
        return binCountCache.getIfPresent(binEpochSeconds);
    }

    public static void putBinCount(BinCount binCount) {
        binCountCache.put(binCount.binEpochSeconds, binCount);
    }
}
