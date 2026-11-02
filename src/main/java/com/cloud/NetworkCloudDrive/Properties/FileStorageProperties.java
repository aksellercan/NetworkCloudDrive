package com.cloud.NetworkCloudDrive.Properties;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.File;

@ConfigurationProperties(prefix = "app.file-storage")
@Component
public class FileStorageProperties {
    private final Logger logger = LoggerFactory.getLogger(FileStorageProperties.class);
    private String basePath;
    private final Environment env;

    public FileStorageProperties(Environment env) {
        this.env = env;
    }

    @PostConstruct
    public void init() {
        String path = env.getProperty("storage-root-path");
        if (path == null) {
            this.basePath = "." + File.separator + "root" + File.separator;
        } else {
            this.basePath = path.trim().replace("\"", "");
            if (!this.basePath.endsWith(File.separator)) {
                this.basePath += File.separator;
            }
        }
        logger.info("Root path set to {}", this.basePath);
    }

    public String getFullPath(String username) {
        return basePath + username;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }
}
