package com.chronomon.st.data.server.controller;

import com.chronomon.st.data.server.catalog.CatalogContext;
import com.chronomon.st.data.server.common.ResponseResult;
import com.chronomon.st.data.server.model.vo.CatalogVO;
import com.chronomon.st.data.server.service.catalog.ICatalogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 用户目录相关接口
 *
 * @author wangrubin
 */
@Slf4j
@RestController
@RequestMapping("/catalog")
public class CatalogController {

    @Resource
    private ICatalogService catalogService;

    /**
     * 初始化用户目录
     *
     * @param param 初始化参数
     * @return 用户目录ID
     */
    @PostMapping("/init")
    public ResponseResult<String> initCatalog(@RequestBody CatalogVO param) {
        return ResponseResult.success(catalogService.initCatalog(param).getCatalogId());
    }

    /**
     * 注销用户目录
     *
     * @return 是否注销成功
     */
    @DeleteMapping("/destroy")
    public ResponseResult<Boolean> destroyCatalog() {
        String catalogId = CatalogContext.getCatalog().getCatalogId();
        return ResponseResult.success(catalogService.destroyCatalogById(catalogId));
    }
}
