package com.chronomon.st.data.server.service.data.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chronomon.st.data.server.dao.OidBatchMapper;
import com.chronomon.st.data.server.model.entity.OidBatchPO;
import com.chronomon.st.data.server.service.data.IOidBatchService;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class OidBatchServiceImpl extends ServiceImpl<OidBatchMapper, OidBatchPO> implements IOidBatchService {
    @Override
    public boolean createTable(String catalogId) {
        return getBaseMapper().createTable("t_user_gps_oid_batch" + "_" + catalogId) > 0;
    }

    @Override
    public boolean dropTable(String catalogId) {
        return getBaseMapper().dropTable("t_user_gps_oid_batch" + "_" + catalogId) > 0;
    }

    @Override
    public OidBatchPO getBatch(Instant periodStartTime, String oid) {
       /* LambdaQueryWrapper<OidBatchPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OidBatchPO::getOid, oid);
        queryWrapper.eq(OidBatchPO::getPeriodStartTime, periodStartTime);
        return getOne(queryWrapper, false);*/
        // todo: 实现好的问题
        return null;
    }
}
