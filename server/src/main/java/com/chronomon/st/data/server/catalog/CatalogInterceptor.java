package com.chronomon.st.data.server.catalog;

import com.chronomon.st.data.server.model.entity.CatalogPO;
import com.chronomon.st.data.server.service.catalog.ICatalogService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用户目录拦截器
 *
 * @author wangrubin
 */
@Component
public class CatalogInterceptor implements HandlerInterceptor {

    @Resource
    private ICatalogService catalogService;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {

        String accessKey = request.getHeader("Access-Key");
        if (accessKey == null || accessKey.isEmpty()) {
            // 用户目录缺失
            return false;
        }

        // 保存用户目录
        CatalogPO catalogPO = catalogService.getByCatalogId(accessKey);
        if (catalogPO != null) {
            // 校验成功
            CatalogContext.saveCatalog(catalogPO);
            return true;
        } else {
            // 找不到与Access-Key对应的用户目录
            return false;
        }
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @Nullable Object handler, Exception ex) {

        // 移除用户目录
        CatalogContext.removeCatalog();
    }
}
