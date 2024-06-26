package com.chronomon.st.data.server.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Instant;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据归档任务的调度器：管理最新的归档时间片，触发异步归档
 *
 * @author wangrubin
 */
@Slf4j
@EnableScheduling
@Service
public class DataRollScheduler {

    @Resource
    private DataRollExecutor dataRollExecutor;

    /**
     * 记录每个用户目录最大的归档时间片
     */
    private static final Map<String, Long> CATALOG_PERIOD_UNTIL_TIME = new ConcurrentHashMap<>();

    /**
     * 注册数据归档的触发
     *
     * @param catalogId 用户目录ID
     * @param untilTime 待归档数据的截至时间
     */
    public void registerTrigger(String catalogId, long untilTime) {
        System.out.println("注册归档触发器：" + Date.from(Instant.ofEpochSecond(untilTime)));
        CATALOG_PERIOD_UNTIL_TIME.compute(catalogId, (key, periodStartTime) -> {
            if (periodStartTime == null) {
                return untilTime;
            } else {
                return Math.max(periodStartTime, untilTime);
            }
        });
    }

    /**
     * 每分钟检测并处理一次归档触发
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void processDataRoll() {
        System.out.println("开启周期同步任务，当前需要同步的数量：" + CATALOG_PERIOD_UNTIL_TIME.size());
        Iterator<Map.Entry<String, Long>> iterator = CATALOG_PERIOD_UNTIL_TIME.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();
            iterator.remove(); // 从注册记录中删除
            String catalogId = entry.getKey();
            Long periodUntilTime = entry.getValue();
            dataRollExecutor.rollData(catalogId, periodUntilTime);
        }
    }
}
