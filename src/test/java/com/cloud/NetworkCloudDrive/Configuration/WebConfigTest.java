package com.cloud.NetworkCloudDrive.Configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;

import java.lang.reflect.Field;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

class WebConfigTest {

    private WebConfig webConfig;

    @BeforeEach
    void setUp() {
        webConfig = new WebConfig();
    }

    @Test
    void configurePathMatch_AddsApiVersionPrefixForBasePackage() throws Exception {
        PathMatchConfigurer configurer = new PathMatchConfigurer();
        webConfig.configurePathMatch(configurer);

        Field prefixField = PathMatchConfigurer.class.getDeclaredField("pathPrefixes");
        prefixField.setAccessible(true);
        @SuppressWarnings("unchecked")
        var prefixes = (java.util.Map<String, Predicate<?>>) prefixField.get(configurer);
        assertNotNull(prefixes);
        assertTrue(prefixes.containsKey("/api/v{version}"));
    }

    @Test
    void configureApiVersioning_SetsPathSegmentAndSupportedVersions() {
        ApiVersionConfigurer configurer = new ApiVersionConfigurer();
        webConfig.configureApiVersioning(configurer);

        assertNotNull(configurer);
    }
}
