package com.chronomon.st.data.server.catalog;

import com.chronomon.st.data.server.model.entity.CatalogPO;

/**
 * 记录线程与用户目录的对应关系
 *
 * @author wangrubin
 */
public class CatalogContext {
    private static final ThreadLocal<CatalogPO> CATALOG_HOLDER = new ThreadLocal<>();

    /**
     * 保存用户目录
     */
    public static void saveCatalog(CatalogPO catalog) {
        CATALOG_HOLDER.set(catalog);
    }

    /**
     * 删除用户目录
     */
    public static void removeCatalog() {
        CATALOG_HOLDER.remove();
    }

    /**
     * 获取用户目录
     */
    public static CatalogPO getCatalog() {
        return CATALOG_HOLDER.get();
    }
}
