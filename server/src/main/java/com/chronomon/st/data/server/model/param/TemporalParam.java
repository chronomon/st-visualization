package com.chronomon.st.data.server.model.param;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.chronomon.st.data.server.model.entity.CatalogPO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * 时间范围查询参数
 *
 * @author wangrubin
 */
@Data
@NoArgsConstructor
public class TemporalParam {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Instant startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Instant endTime;

    @JsonIgnore
    private List<Instant> periodTimeList;

    public TemporalParam(Instant startTime, Instant endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public void bindCatalog(CatalogPO catalogPO) {
        ChronoUnit periodUnit = ChronoUnit.valueOf(catalogPO.getPeriodUnit());
        this.periodTimeList = new ArrayList<>();
        Instant startPeriod = startTime.truncatedTo(periodUnit);
        Instant endPeriod = endTime.truncatedTo(periodUnit);
        periodTimeList.add(startPeriod);
        Instant periodTime = startPeriod;
        while (periodTime.isBefore(endPeriod)) {
            periodTime = periodTime.plusSeconds(periodUnit.getDuration().getSeconds());
            periodTimeList.add(periodTime);
        }
    }

    public boolean isMinGrainSize() {
        return periodTimeList.size() == 1;
    }

    public Instant getStartPeriod() {
        return periodTimeList.get(0);
    }

    public Instant getEndPeriod() {
        return periodTimeList.get(periodTimeList.size() - 1);
    }
}