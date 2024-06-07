package com.chronomon.st.data.model.feature;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * 时空对象基础信息
 *
 * @author wangrubin
 */
public class BaseFeature {

    // 对象ID
    public final String oid;

    // 时间戳
    public final Instant time;

    public BaseFeature(String oid, Instant time) {
        this.oid = oid;
        this.time = time;
    }

    /**
     * 将时空对象落位到对应时间片(用时间片的起始时刻表示)
     *
     * @param periodUnit 时间片单位
     * @return 时间片起始时刻
     */
    public Instant locate2Period(ChronoUnit periodUnit) {
        return this.time.truncatedTo(periodUnit);
    }
}
