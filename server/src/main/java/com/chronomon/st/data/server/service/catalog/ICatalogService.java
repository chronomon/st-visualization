package com.chronomon.st.data.server.service.catalog;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chronomon.st.data.server.model.entity.CatalogPO;

public interface ICatalogService extends IService<CatalogPO> {

    CatalogPO getByAccessKey(String accessKey);
}
