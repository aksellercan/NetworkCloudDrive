package com.cloud.NetworkCloudDrive.Configuration;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Field;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;

class AsyncConfiguratorTest {

    @Test
    void taskExecutor_CreatesConfiguredThreadPool_WithDefaults() throws Exception {
        AsyncConfigurator configurator = new AsyncConfigurator(new MockEnvironment());
        Executor executor = configurator.taskExecutor();

        assertNotNull(executor);
        assertTrue(executor instanceof ThreadPoolTaskExecutor);
        ThreadPoolTaskExecutor tpe = (ThreadPoolTaskExecutor) executor;

        Field coreField = ThreadPoolTaskExecutor.class.getDeclaredField("corePoolSize");
        coreField.setAccessible(true);
        Field maxField = ThreadPoolTaskExecutor.class.getDeclaredField("maxPoolSize");
        maxField.setAccessible(true);
        Field queueField = ThreadPoolTaskExecutor.class.getDeclaredField("queueCapacity");
        queueField.setAccessible(true);

        assertEquals(5, coreField.getInt(tpe));
        assertEquals(10, maxField.getInt(tpe));
        assertEquals(100, queueField.getInt(tpe));
    }

    @Test
    void taskExecutor_UsesCustomPropertiesFromEnvironment() throws Exception {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("thread-set-core-pool-size", "2");
        env.setProperty("thread-set-max-pool-size", "4");
        env.setProperty("thread-set-queue-capacity", "50");
        env.setProperty("thread-set-name-prefix", "\"async-test-\"");

        AsyncConfigurator configurator = new AsyncConfigurator(env);
        ThreadPoolTaskExecutor tpe = (ThreadPoolTaskExecutor) configurator.taskExecutor();

        assertEquals(2, tpe.getCorePoolSize());
        assertEquals(4, tpe.getMaxPoolSize());
        assertEquals(50, tpe.getQueueCapacity());
        assertEquals("async-test-", tpe.getThreadNamePrefix());
    }
}
