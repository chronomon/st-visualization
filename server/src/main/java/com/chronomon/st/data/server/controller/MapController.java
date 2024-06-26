package com.chronomon.st.data.server.controller;

import com.chronomon.st.data.server.catalog.CatalogContext;
import com.chronomon.st.data.server.model.param.TileTemporalQueryParam;
import com.chronomon.st.data.server.service.application.ITileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;

/**
 * 地图服务相关接口
 *
 * @author wangrubin
 */
@Slf4j
@RestController
@RequestMapping("/map")
public class MapController {

    @Resource
    private ITileService tileService;

    @GetMapping("/tile/{z}/{y}/{x}.pbf")
    public void projectTile(@PathVariable("z") Integer z,
                            @PathVariable("y") Integer y,
                            @PathVariable("x") Integer x,
                            HttpServletResponse response) {
        try {
            response.setContentType("application/x-protobuf");
            response.setCharacterEncoding("utf-8");
            // 这里URLEncoder.encode可以防止中文乱码
            String encodedFileName = URLEncoder.encode(x.toString(), StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename*=utf-8''" + encodedFileName + ".pbf");

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Instant startTime = dateFormat.parse("2008-02-03 10:00:00").toInstant();
            Instant endTime = dateFormat.parse("2008-02-04 12:00:00").toInstant();

            TileTemporalQueryParam param = new TileTemporalQueryParam(startTime, endTime, z, x, y);
            param.bindCatalog(CatalogContext.getCatalog());

            byte[] vectorTile = tileService.getTile(param);
            response.getOutputStream().write(vectorTile);
        } catch (Exception e) {
            // 重置response
            log.error("文件下载失败" + e.getMessage());
            throw new RuntimeException("下载文件失败", e);
        }
    }
}
