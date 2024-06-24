package com.chronomon.st.data.server.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.Resource;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置
 * 1. 注册包装器，实现父线程到子线程的MDC拷贝
 * 2. 根据配置文件中的值设置线程池参数
 */
@Configuration
@EnableAsync
@Slf4j
public class ThreadPoolTaskConfig {

    @Resource
    private TaskExecutionProperties taskExecutionProperties;

    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 从配置文件中读取线程池相关配置参数并设置
        executor.setThreadNamePrefix(taskExecutionProperties.getThreadNamePrefix());
        executor.setCorePoolSize(taskExecutionProperties.getPool().getCoreSize());
        executor.setQueueCapacity(taskExecutionProperties.getPool().getQueueCapacity());
        executor.setMaxPoolSize(taskExecutionProperties.getPool().getMaxSize());
        executor.setKeepAliveSeconds((int) taskExecutionProperties.getPool().getKeepAlive().getSeconds());
        executor.setAllowCoreThreadTimeOut(taskExecutionProperties.getPool().isAllowCoreThreadTimeout());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // 初始化
        executor.initialize();

        return executor;
    }
}
