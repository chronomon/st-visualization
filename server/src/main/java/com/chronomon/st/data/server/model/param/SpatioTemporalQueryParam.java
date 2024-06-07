package com.chronomon.st.data.server.model.param;

import lombok.Data;

/**
 * 空间 + 时间范围查询参数
 *
 * @author wangrubin
 */
@Data
public class SpatioTemporalQueryParam extends TemporalParam {

    private double minLon;

    private double maxLon;

    private double minLat;

    private double maxLat;
}
