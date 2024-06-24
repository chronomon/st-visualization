package com.chronomon.st.data.server.service.data.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chronomon.st.data.model.feature.MapFeature;
import com.chronomon.st.data.server.dao.RawDataMapper;
import com.chronomon.st.data.server.model.entity.RawDataPO;
import com.chronomon.st.data.server.service.data.IRawDataService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class RawDataServiceImpl extends ServiceImpl<RawDataMapper, RawDataPO> implements IRawDataService {

    @Override
    public boolean createTable(String catalogId) {
        return getBaseMapper().createTable("t_user_gps_raw" + "_" + catalogId) > 0;
    }

    @Override
    public boolean dropTable(String catalogId) {
        return getBaseMapper().dropTable("t_user_gps_raw" + "_" + catalogId) > 0;
    }

    @Override
    public List<MapFeature> getFeatures(String catalogId, Instant periodStartTime, Instant periodEndTime) {
        return null;
    }
}
