package com.cloud.NetworkCloudDrive.Configuration;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Field;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;

class AsyncConfiguratorTest {

    @Test
    void taskExecutor_CreatesConfiguredThreadPool() throws Exception {
        AsyncConfigurator configurator = new AsyncConfigurator();
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
}
