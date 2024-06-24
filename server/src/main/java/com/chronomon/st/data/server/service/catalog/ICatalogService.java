package com.chronomon.st.data.server.service.catalog;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chronomon.st.data.server.model.entity.CatalogPO;
import com.chronomon.st.data.server.model.vo.CatalogVO;

public interface ICatalogService extends IService<CatalogPO> {

    CatalogPO initCatalog(CatalogVO param);

    boolean destroyCatalog(String catalogId);

    boolean catalogExists(String catalogId);

    CatalogPO getByCatalogId(String accessKey);
}
