package com.jd.st.data.storage.temporal;

import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * 时间片元信息
 */
public class Period implements Serializable {

    /**
     * 每小时一个时间片
     *
     * @return 所属时间片起始时间戳
     */
    public static Instant ofHour(Instant time) {
        return time.truncatedTo(ChronoUnit.HOURS);
    }

    /**
     * 每天一个时间片
     *
     * @return 所属时间片起始时间戳
     */
    public static Instant ofDay(Instant time) {
        return time.truncatedTo(ChronoUnit.DAYS);
    }

    /**
     * 每周一个时间片
     *
     * @return 所属时间片起始时间戳
     */
    public static Instant ofWeek(Instant time) {
        return time.truncatedTo(ChronoUnit.WEEKS);
    }

    /**
     * 每月一个时间片
     *
     * @return 所属时间片起始时间戳
     */
    public static Instant ofMonth(Instant time) {
        return time.truncatedTo(ChronoUnit.MONTHS);
    }
}
