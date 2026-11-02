package com.cloud.NetworkCloudDrive.Utilities;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

@Component
public class APIComponent {
    private final BuildProperties buildProperties;
    private final Logger logger = LoggerFactory.getLogger(APIComponent.class);

    public APIComponent(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    public String getAPIVersion() {
        return buildProperties.getVersion();
    }

    @PostConstruct
    public void logAPIVersionOnStartup() {
        String version = getAPIVersion();
        String message = "";
        if (version.contains("SNAPSHOT")) {
            message = "Development Build";
        }
        logger.info("API Version: {} {}", version, message);
    }
}
