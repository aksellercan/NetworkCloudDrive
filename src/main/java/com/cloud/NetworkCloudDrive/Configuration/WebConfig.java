package com.cloud.NetworkCloudDrive.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final Environment env;
    private final Logger logger = LoggerFactory.getLogger(WebConfig.class);

    public WebConfig(Environment env) {
        this.env = env;
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // api versioning
        configurer.addPathPrefix("api/v{version}", HandlerTypePredicate.forBasePackage("com.cloud.NetworkCloudDrive"));
    }

    @Override
    public void configureApiVersioning(ApiVersionConfigurer configurer) {
        String[] allowedVersions = env.getProperty("api-supported-versions", "\"1.0\", \"2.0\", \"3.0\"").replace("\"", "").split(",");
        String defaultVersion = env.getProperty("api-default-version", "1.0").replace("\"", "");
        configurer
                .usePathSegment(1)
                .setDefaultVersion(defaultVersion)
                .addSupportedVersions(allowedVersions);
        logger.info("API default endpoint version {} and allowed versions {}", defaultVersion, allowedVersions);
    }
}
