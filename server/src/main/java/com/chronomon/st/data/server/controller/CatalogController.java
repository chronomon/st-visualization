package com.chronomon.st.data.server.controller;

import com.chronomon.st.data.server.common.ResponseResult;
import com.chronomon.st.data.server.model.param.OidTemporalQueryParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户目录相关接口
 *
 * @author wangrubin
 */
@Slf4j
@RestController
@RequestMapping("/catalog")
public class CatalogController {

    /**
     * 初始化用户目录
     *
     * @param param 初始化参数
     * @return 用户目录ID
     */
    @PostMapping("/init")
    public ResponseResult<String> initCatalog(@RequestBody OidTemporalQueryParam param) {
        return null;
    }

    /**
     * 注销用户目录
     *
     * @param catalogId 用户目录ID
     * @return 是否注销成功
     */
    @DeleteMapping("/destroy")
    public ResponseResult<Boolean> destroyCatalog(@RequestParam String catalogId) {
        return null;
    }

    /**
     * 判断用户目录是否存在
     *
     * @param catalogId 用户目录ID
     * @return 目录是否存在
     */
    @GetMapping("/exists")
    public ResponseResult<Boolean> catalogExists(@RequestParam String catalogId) {
        return null;
    }
}
