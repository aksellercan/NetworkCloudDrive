package com.cloud.NetworkCloudDrive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
//default caching behaviour for now
@EnableCaching
@EnableScheduling
public class NetworkCloudDriveApplication {
    public static void main(String[] args) {
        final Logger logger = LoggerFactory.getLogger(NetworkCloudDriveApplication.class);
        logger.info("System Information:\nOperating System: {}\nOperating System Version: {}\nOperating System Architecture: {}", System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch"));
        SpringApplication.run(NetworkCloudDriveApplication.class, args);
    }
}
