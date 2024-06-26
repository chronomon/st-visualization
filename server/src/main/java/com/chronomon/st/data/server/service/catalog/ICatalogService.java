package com.chronomon.st.data.server.service.catalog;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chronomon.st.data.server.model.entity.CatalogPO;
import com.chronomon.st.data.server.model.vo.CatalogVO;

public interface ICatalogService extends IService<CatalogPO> {

    CatalogPO initCatalog(CatalogVO param);

    boolean destroyCatalogById(String catalogId);

    boolean destroyCatalogByName(String catalogName);

    boolean updateCatalog(CatalogPO catalogPO);

    boolean catalogExists(String catalogId);

    CatalogPO getCatalogById(String catalogId);
    CatalogPO getCatalogByName(String catalogName);
}
