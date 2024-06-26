package com.chronomon.st.data.server.service.application.impl;

import com.chronomon.st.data.model.feature.GeodeticFeature;
import com.chronomon.st.data.model.feature.MapFeature;
import com.chronomon.st.data.model.pyramid.MapDescriptor;
import com.chronomon.st.data.server.catalog.CatalogContext;
import com.chronomon.st.data.server.model.bo.Trajectory;
import com.chronomon.st.data.server.model.entity.CatalogPO;
import com.chronomon.st.data.server.model.param.OidTemporalQueryParam;
import com.chronomon.st.data.server.service.application.ITrajectoryService;
import com.chronomon.st.data.server.service.data.IOidBatchService;
import com.chronomon.st.data.server.service.data.IOidStatisticService;
import com.chronomon.st.data.server.service.data.IRawDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TrajectoryServiceImpl implements ITrajectoryService {

    @Resource
    private IRawDataService rawDataService;

    @Resource
    private IOidBatchService oidBatchService;

    @Resource
    private IOidStatisticService oidStatisticService;

    @Override
    public Trajectory queryByOidTemporal(OidTemporalQueryParam param) {
        CatalogPO catalogPO = CatalogContext.getCatalog();
        List<MapFeature> featureList = new ArrayList<>();

        if (catalogPO.getNextRollPeriod() > param.getStartTime().getEpochSecond()) {
            // 查询已归档数据
            featureList.addAll(oidBatchService.getFeaturesByTemporal(param));
        }

        if (catalogPO.getNextRollPeriod() <= param.getEndTime().getEpochSecond()) {
            // 查找待归档数据
            featureList.addAll(rawDataService.getFeaturesByOid(param.getOid()));
        }

        return new Trajectory(param.getOid(), toGeodeticFeatures(featureList), true);
    }

    private List<GeodeticFeature> toGeodeticFeatures(List<MapFeature> mapFeatureList) {
        CatalogPO catalogPO = CatalogContext.getCatalog();
        MapDescriptor mapDescriptor = new MapDescriptor(catalogPO.getMaxZoomLevel(), catalogPO.getTileExtent());
        return mapFeatureList.stream()
                .map(mapFeature -> mapDescriptor.convert2ProjectFeature(mapFeature).toGeodeticFeature())
                .collect(Collectors.toList());
    }
}
