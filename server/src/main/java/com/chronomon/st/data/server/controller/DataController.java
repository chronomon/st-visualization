package com.chronomon.st.data.server.controller;

import com.chronomon.st.data.server.model.param.OidTemporalQueryParam;
import com.chronomon.st.data.server.model.param.SpatioTemporalQueryParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 数据服务相关接口
 *
 * @author wangrubin
 */
@Slf4j
@RestController
@RequestMapping("/data")
public class DataController {

    /**
     * OID + 时间范围查询
     *
     * @param param 查询参数
     */
    @PostMapping("/queryByOidTemporal")
    public void queryByOidTemporal(@RequestBody OidTemporalQueryParam param) {

    }

    /**
     * 空间 + 时间范围查询
     *
     * @param param 查询参数
     */
    @PostMapping("/queryBySpatioTemporal")
    public void queryBySpatioTemporal(@RequestBody SpatioTemporalQueryParam param) {

    }
}
