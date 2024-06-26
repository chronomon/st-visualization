package com.chronomon.st.data.server.service.application;

import com.chronomon.st.data.model.feature.GeodeticFeature;
import com.chronomon.st.data.model.feature.MapFeature;
import com.chronomon.st.data.model.pyramid.MapDescriptor;
import com.chronomon.st.data.server.STVisualizationApp;
import com.chronomon.st.data.server.catalog.CatalogContext;
import com.chronomon.st.data.server.model.entity.CatalogPO;
import com.chronomon.st.data.server.model.param.OidTemporalQueryParam;
import com.chronomon.st.data.server.model.vo.CatalogVO;
import com.chronomon.st.data.server.service.catalog.ICatalogService;
import com.chronomon.st.data.server.service.data.IRawDataService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.annotation.Resource;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest(classes = {STVisualizationApp.class})
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
public class TileServiceTest {

    @Resource
    private ICatalogService catalogService;

    @Resource
    private IRawDataService rawDataService;

    @Resource
    private ITrajectoryService trajectoryService;

    private static final String CATALOG_NAME = "测试用户目录1";

    private static final String START_TIME = "2008-02-03 00:00:00";

    private static final String END_TIME = "2008-02-04 00:00:00";

    @Test
    public void testInitData() {
        // 删除库中已存在的用户目录
        catalogService.destroyCatalogByName(CATALOG_NAME);

        // 初始化用户目录与对应的用户表
        CatalogVO catalogParam = new CatalogVO();
        catalogParam.setCatalogName(CATALOG_NAME);
        catalogParam.setMaxZoomLevel(18);
        catalogParam.setTileExtent(512);
        catalogParam.setPeriodUnit("Hours");
        CatalogPO catalogPO = catalogService.initCatalog(catalogParam);

        // 设置当前线程的用户目录
        CatalogContext.saveCatalog(catalogPO);

        // 金字塔最底层地图
        MapDescriptor mapDescriptor = new MapDescriptor(catalogPO.getMaxZoomLevel(), catalogPO.getTileExtent());
        String fileName = "gps_data.csv";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try (InputStream in = this.getClass().getClassLoader().getResourceAsStream(fileName);
             Reader reader = new InputStreamReader(in);
             BufferedReader bReader = new BufferedReader(reader)) {
            List<MapFeature> featureList = new ArrayList<>();

            String line;
            while ((line = bReader.readLine()) != null) {
                String[] attrs = line.split(",");
                String oid = attrs[0];
                Instant time = dateFormat.parse(attrs[1]).toInstant();
                double lon = Double.parseDouble(attrs[2]);
                double lat = Double.parseDouble(attrs[3]);

                MapFeature mapFeature = mapDescriptor.convert2MapFeature(new GeodeticFeature(oid, time, lon, lat));
                featureList.add(mapFeature);

                //一分钟导入120w条
                if (featureList.size() >= 10000) {
                    rawDataService.saveFeatures(featureList);
                    featureList = new ArrayList<>();
                    Thread.sleep(500); // 睡0.5秒
                }
            }
            rawDataService.saveFeatures(featureList);

            // 再睡觉一分钟，让最后的归档任务执行
            Thread.sleep(60 * 1000);
        } catch (Exception e) {
            throw new RuntimeException("读取文件失败", e);
        }

        System.out.println("========================================================");
        System.out.println("用户目录ID:" + catalogPO.getCatalogId());
        String vectorTileUrl = "http://localhost:8080/st-visualization/map/tile/{z}/{y}/{x}.pbf?catalogId=" + catalogPO.getCatalogId();
        System.out.println("瓦片请求URL:" + vectorTileUrl);
    }

    @Test
    public void testTrajectory() throws ParseException {
        CatalogPO catalogPO = catalogService.getCatalogByName(CATALOG_NAME);
        CatalogContext.saveCatalog(catalogPO);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Instant startTime = dateFormat.parse(START_TIME).toInstant();
        Instant endTime = dateFormat.parse(END_TIME).toInstant();
        OidTemporalQueryParam param = new OidTemporalQueryParam();
        param.setOid("6275");
        param.setStartTime(startTime);
        param.setEndTime(endTime);
        param.bindCatalog(catalogPO);

        System.out.println(trajectoryService.queryByOidTemporal(param).size());
    }

    @Test
    public void testDestroyCatalog() {
        catalogService.destroyCatalogByName(CATALOG_NAME);
    }
}