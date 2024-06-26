package com.chronomon.st.data.server.service.application;


import com.chronomon.st.data.server.model.bo.Trajectory;
import com.chronomon.st.data.server.model.param.OidTemporalQueryParam;

public interface ITrajectoryService {

    /**
     * OID + 时间范围查询一条轨迹
     *
     * @param param 查询参数
     * @return 一条轨迹
     */
    Trajectory queryByOidTemporal(OidTemporalQueryParam param);
}
