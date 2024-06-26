package com.chronomon.st.data.server.service.data;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chronomon.st.data.model.collection.PeriodOidCollection;
import com.chronomon.st.data.model.feature.MapFeature;
import com.chronomon.st.data.server.model.entity.OidBatchPO;
import com.chronomon.st.data.server.model.param.OidTemporalQueryParam;

import java.util.List;

public interface IOidBatchService extends IService<OidBatchPO> {

    void createTable(String catalogId);

    void dropTable(String catalogId);

    void saveFeatures(List<PeriodOidCollection> oidCollectionList);

    List<MapFeature> getFeaturesByTemporal(OidTemporalQueryParam param);
}
