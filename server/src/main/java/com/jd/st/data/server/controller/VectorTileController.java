package com.jd.st.data.server.controller;

import com.jd.st.data.server.service.ITileService;
import com.jd.st.data.storage.tile.TileCoord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("/visualization")
public class VectorTileController {

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

            TileCoord tileCoord = new TileCoord(y, x, z);
            byte[] vectorTile = tileService.getTile(tileCoord);
            response.getOutputStream().write(vectorTile);
        } catch (Exception e) {
            // 重置response
            log.error("文件下载失败" + e.getMessage());
            throw new RuntimeException("下载文件失败", e);
        }
    }
}
