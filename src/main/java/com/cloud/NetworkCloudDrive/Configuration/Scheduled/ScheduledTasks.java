package com.cloud.NetworkCloudDrive.Configuration.Scheduled;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ScheduledTasks {
    private final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);

    public ScheduledTasks() {
    }

    @Scheduled(fixedDelay = 2 * 3600000)
    public void heartbeat() {
        Instant now = Instant.now();
        logger.info("Heartbeat {}", now);
    }
}
