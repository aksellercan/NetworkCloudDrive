package com.cloud.NetworkCloudDrive.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfigurator implements AsyncConfigurer {
    private final Environment env;
    private final Logger logger = LoggerFactory.getLogger(AsyncConfigurator.class);

    public AsyncConfigurator(Environment env) {
        this.env = env;
    }

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(Integer.parseInt(env.getProperty("thread-set-core-pool-size", "5")));
        executor.setMaxPoolSize(Integer.parseInt(env.getProperty("thread-set-max-pool-size", "10")));
        executor.setQueueCapacity(Integer.parseInt(env.getProperty("thread-set-queue-capacity", "100")));
        executor.setThreadNamePrefix(env.getProperty("thread-set-name-prefix", "async-worker-").replace("\"", ""));
        executor.initialize();
        logger.info("Simple Task Executor Configuration:\nCore pool size: {}\nMax pool size: {}\nQueue capacity: {}\nName Prefix: {}", executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity(), executor.getThreadNamePrefix());
        return executor;
    }
}
