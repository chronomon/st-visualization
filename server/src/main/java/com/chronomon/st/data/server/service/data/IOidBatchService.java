package com.chronomon.st.data.server.service.data;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chronomon.st.data.server.model.entity.OidBatchPO;

import java.time.Instant;

public interface IOidBatchService extends IService<OidBatchPO> {

    boolean createTable(String catalogName);

    OidBatchPO getBatch(Instant periodStartTime, String oid);
}
