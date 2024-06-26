package com.chronomon.st.data.server.service.data.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chronomon.st.data.model.collection.PeriodOidCollection;
import com.chronomon.st.data.model.feature.MapFeature;
import com.chronomon.st.data.server.dao.OidBatchMapper;
import com.chronomon.st.data.server.model.entity.OidBatchPO;
import com.chronomon.st.data.server.model.param.OidTemporalQueryParam;
import com.chronomon.st.data.server.service.data.IOidBatchService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OidBatchServiceImpl extends ServiceImpl<OidBatchMapper, OidBatchPO> implements IOidBatchService {

    // 模板表名
    private static final String TEMPLATE_TABLE = "t_template_gps_batch";

    // 用户表名前缀
    private static final String USER_TABLE_PREFIX = "t_user_gps_oid_batch_";

    @Resource
    private DynamicTableService dynamicTableService;

    @Override
    public void createTable(String catalogId) {
        dynamicTableService.createTable(USER_TABLE_PREFIX + catalogId, TEMPLATE_TABLE);
    }

    @Override
    public void dropTable(String catalogId) {
        dynamicTableService.dropTable(USER_TABLE_PREFIX + catalogId);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveFeatures(List<PeriodOidCollection> oidCollectionList) {
        List<OidBatchPO> oidBatchPOList = new ArrayList<>(oidCollectionList.size());
        for (PeriodOidCollection oidCollection : oidCollectionList) {
            String combineIndex = oidCollection.oid + "_" + oidCollection.periodStartTime.getEpochSecond();
            byte[] featureBytes = oidCollection.serializeFeatures();

            OidBatchPO oidBatchPO = new OidBatchPO();
            oidBatchPO.setCombineIndex(combineIndex);
            oidBatchPO.setDataBatch(featureBytes);

            oidBatchPOList.add(oidBatchPO);
        }

        saveBatch(oidBatchPOList);
    }

    @Override
    public List<MapFeature> getFeaturesByTemporal(OidTemporalQueryParam param) {
        // 构造索引值
        List<String> indexList = param.getPeriodTimeList().stream()
                .map(periodStartTime -> param.getOid() + "_" + periodStartTime.getEpochSecond())
                .collect(Collectors.toList());
        if (indexList.isEmpty()) {
            return Collections.emptyList();
        }

        // 查询数据包
        LambdaQueryWrapper<OidBatchPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(OidBatchPO::getCombineIndex, indexList);
        queryWrapper.orderByAsc(OidBatchPO::getCombineIndex);
        List<OidBatchPO> oidBatchPOList = list(queryWrapper);

        // 解析时空对象
        return oidBatchPOList.stream()
                .flatMap(oidBatchPO -> oidBatchPO.toCollection().getFeatureList().stream())
                .collect(Collectors.toList());
    }
}
