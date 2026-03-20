package com.alicp.jetcache.external;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

public class ExternalCacheConfigTest {

    @Test
    public void testCloneShouldCopyWriteInterceptorList() {
        ExternalCacheConfig<Object, Object> config = new ExternalCacheConfig<>();
        List<ExternalCacheWriteInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new LoggingExternalCacheWriteInterceptor());
        config.setWriteInterceptors(interceptors);

        ExternalCacheConfig<Object, Object> cloned = config.clone();
        assertNotSame(config.getWriteInterceptors(), cloned.getWriteInterceptors());
        assertEquals(1, config.getWriteInterceptors().size());
        assertEquals(1, cloned.getWriteInterceptors().size());

        cloned.addWriteInterceptor(new BigKeyDetectExternalCacheWriteInterceptor());
        assertEquals(1, config.getWriteInterceptors().size());
        assertEquals(2, cloned.getWriteInterceptors().size());
    }
}
