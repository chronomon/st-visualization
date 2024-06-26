package com.chronomon.st.data.server.compnent;

import com.chronomon.st.data.server.model.entity.CatalogPO;
import com.chronomon.st.data.server.service.catalog.ICatalogService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 用户目录缓存：单实例时使用内存，多实例时使用redis
 */
@Component
public class CatalogHolder {

    @Resource
    private ICatalogService catalogService;

    private static final long MAX_CACHE_COUNT = 500;

    private static final Cache<String, CatalogPO> CATALOG_CACHE =
            Caffeine.newBuilder()
                    .expireAfterAccess(30, TimeUnit.MINUTES)
                    .maximumSize(MAX_CACHE_COUNT).build();

    /**
     * 从数据库或缓存中获取用户目录
     *
     * @param catalogId 用户目录ID
     */
    public CatalogPO getCatalog(String catalogId) {
        return CATALOG_CACHE.get(catalogId, key -> catalogService.getCatalogById(key));
    }

    /**
     * 从缓存中清除失效的用户目录
     *
     * @param catalogId 用户目录ID
     */
    public void cleanCache(String catalogId) {
        CATALOG_CACHE.invalidate(catalogId);
    }
}
