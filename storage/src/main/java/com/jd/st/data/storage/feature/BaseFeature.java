package com.jd.st.data.storage.feature;

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
    public Instant time;

    public BaseFeature(String oid, Instant time) {
        this.oid = oid;
        this.time = time;
    }

    /**
     * 按照时间片截取，将绝对时间戳转为时间片内的相对时间戳
     *
     * @param periodUnit 时间片单位
     * @return 时间片起始时间戳
     */
    public Instant truncate(ChronoUnit periodUnit, boolean usePeriodTime) {
        Instant periodStartTime = this.time.truncatedTo(periodUnit);
        if (usePeriodTime) {
            // 将当前时间替换成时间片内的时间
            this.time = this.time.minusSeconds(periodStartTime.getEpochSecond());
        }
        return periodStartTime;
    }
}
