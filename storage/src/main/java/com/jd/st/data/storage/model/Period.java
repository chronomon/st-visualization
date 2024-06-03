package com.jd.st.data.storage.model;

import java.io.Serializable;
import java.time.temporal.ChronoUnit;

public class Period implements Serializable {

    private final long duration;

    public Period(int hours) {
        this.duration = ChronoUnit.HOURS.getDuration().getSeconds() * hours;
    }

    public long binMinEpochSeconds(long epochSeconds) {
        return (epochSeconds / duration) * duration;
    }

    public long binDurationSeconds() {
        return this.duration;
    }
}
