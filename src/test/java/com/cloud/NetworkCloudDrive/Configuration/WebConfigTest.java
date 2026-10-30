package com.cloud.NetworkCloudDrive.Configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

class WebConfigTest {

    private MockEnvironment env;
    private WebConfig webConfig;

    @BeforeEach
    void setUp() {
        env = new MockEnvironment();
        webConfig = new WebConfig(env);
    }

    @Test
    void configurePathMatch_AddsApiVersionPrefixForBasePackage() throws Exception {
        PathMatchConfigurer configurer = new PathMatchConfigurer();
        webConfig.configurePathMatch(configurer);

        Field prefixField = PathMatchConfigurer.class.getDeclaredField("pathPrefixes");
        prefixField.setAccessible(true);
        @SuppressWarnings("unchecked")
        var prefixes = (java.util.Map<String, Predicate<Class<?>>>) prefixField.get(configurer);
        assertNotNull(prefixes);
        assertTrue(prefixes.containsKey("api/v{version}"));
    }

    @Test
    void configureApiVersioning_UsesDefaults_WhenNoPropertiesSet() throws Exception {
        ApiVersionConfigurer configurer = new ApiVersionConfigurer();
        webConfig.configureApiVersioning(configurer);

        assertNotNull(configurer);

        Field versionResolversField = ApiVersionConfigurer.class.getDeclaredField("versionResolvers");
        versionResolversField.setAccessible(true);
        var versionResolvers = (java.util.List<?>) versionResolversField.get(configurer);
        assertEquals(1, versionResolvers.size());

        Field defaultVersionField = ApiVersionConfigurer.class.getDeclaredField("defaultVersion");
        defaultVersionField.setAccessible(true);
        assertEquals("1.0", defaultVersionField.get(configurer));

        Field supportedVersionsField = ApiVersionConfigurer.class.getDeclaredField("supportedVersions");
        supportedVersionsField.setAccessible(true);
        var supportedVersions = (java.util.Collection<?>) supportedVersionsField.get(configurer);
        assertEquals(Set.of("1.0", "2.0", "3.0"),
                supportedVersions.stream().map(v -> v.toString().trim()).collect(java.util.stream.Collectors.toSet()));
    }

    @Test
    void configureApiVersioning_ReadsVersionsFromEnvironment() throws Exception {
        env.setProperty("api-default-version", "\"2.0\"");
        env.setProperty("api-supported-versions", "\"1.0\", \"2.0\"");

        ApiVersionConfigurer configurer = new ApiVersionConfigurer();
        webConfig.configureApiVersioning(configurer);

        Field defaultVersionField = ApiVersionConfigurer.class.getDeclaredField("defaultVersion");
        defaultVersionField.setAccessible(true);
        assertEquals("2.0", defaultVersionField.get(configurer));

        Field supportedVersionsField = ApiVersionConfigurer.class.getDeclaredField("supportedVersions");
        supportedVersionsField.setAccessible(true);
        var supportedVersions = (java.util.Collection<?>) supportedVersionsField.get(configurer);
        assertEquals(Set.of("1.0", "2.0"),
                supportedVersions.stream().map(v -> v.toString().trim()).collect(java.util.stream.Collectors.toSet()));
    }
}
