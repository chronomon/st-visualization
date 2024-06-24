package com.chronomon.st.data.server.service.data;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chronomon.st.data.model.feature.MapFeature;
import com.chronomon.st.data.server.model.entity.RawDataPO;

import java.time.Instant;
import java.util.List;

public interface IRawDataService extends IService<RawDataPO> {

    boolean createTable(String catalogId);

    boolean dropTable(String catalogId);

    List<MapFeature> getFeatures(String catalogId, Instant periodStartTime, Instant periodEndTime);
}
