package com.chronomon.st.data.server.service.data;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chronomon.st.data.model.feature.MapFeature;
import com.chronomon.st.data.model.statistic.TileStatistic;
import com.chronomon.st.data.server.model.entity.RawDataPO;

import java.util.List;

public interface IRawDataService extends IService<RawDataPO> {

    void createTable(String catalogId);

    void dropTable(String catalogId);

    boolean saveFeatures(List<MapFeature> features);

    TileStatistic getTileStatistic(long minZVal, long maxZVal);

    List<MapFeature> getFeaturesByOid(String oid);

    List<MapFeature> getFeaturesByZRange(long minZVal, long maxZVal);

    List<MapFeature> getFeaturesByUntilTime(long untilTime);

    boolean removeByTime(long untilTime);
}
