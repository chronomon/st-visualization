package com.chronomon.st.data.server.controller;

import com.chronomon.st.data.server.common.ResponseResult;
import com.chronomon.st.data.server.model.param.OidTemporalQueryParam;
import com.chronomon.st.data.server.service.application.ITrajectoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 数据服务相关接口
 *
 * @author wangrubin
 */
@Slf4j
@RestController
@RequestMapping("/data")
public class DataController {

    @Resource
    private ITrajectoryService trajectoryService;

    /**
     * OID + 时间范围查询
     *
     * @param param 查询参数
     */
    @PostMapping("/queryByOidTemporal")
    public ResponseResult<String> queryByOidTemporal(@RequestBody OidTemporalQueryParam param) {
        String geoJson = trajectoryService.queryByOidTemporal(param).toJson();
        return ResponseResult.success(geoJson);
    }
}
