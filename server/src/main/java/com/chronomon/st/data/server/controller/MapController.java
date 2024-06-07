package com.chronomon.st.data.server.controller;

import com.chronomon.st.data.server.catalog.CatalogContext;
import com.chronomon.st.data.server.model.param.TileTemporalQueryParam;
import com.chronomon.st.data.server.model.entity.CatalogPO;
import com.chronomon.st.data.server.service.tile.ITileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * 地图服务相关接口
 *
 * @author wangrubin
 */
@Slf4j
@RestController
@RequestMapping("/visualization")
public class MapController {

    @Resource
    private ITileService tileService;

    @GetMapping("/tile/{z}/{y}/{x}.pbf")
    public void projectTile(@PathVariable Integer z,
                            @PathVariable Integer y,
                            @PathVariable Integer x,
                            HttpServletResponse response) {
        try {
            response.setContentType("application/x-protobuf");
            response.setCharacterEncoding("utf-8");
            // 这里URLEncoder.encode可以防止中文乱码
            String encodedFileName = URLEncoder.encode(x.toString(), StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename*=utf-8''" + encodedFileName + ".pbf");

            Instant endTime = Instant.now();
            Instant startTime = endTime.minusSeconds(ChronoUnit.YEARS.getDuration().getSeconds());

            CatalogPO catalogPO = CatalogContext.getCatalog();
            TileTemporalQueryParam param = new TileTemporalQueryParam(startTime, endTime, z, x, y);
            param.bindCatalog(catalogPO);

            byte[] vectorTile = tileService.getTile(param);
            response.getOutputStream().write(vectorTile);
        } catch (Exception e) {
            // 重置response
            log.error("文件下载失败" + e.getMessage());
            throw new RuntimeException("下载文件失败", e);
        }
    }
}
