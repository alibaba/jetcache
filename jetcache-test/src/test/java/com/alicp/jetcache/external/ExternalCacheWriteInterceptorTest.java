
package com.alicp.jetcache.external;

import com.alicp.jetcache.CacheResult;
import com.alicp.jetcache.CacheResultCode;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author youjie_li
 */

public class ExternalCacheWriteInterceptorTest {

    @Test
    public void testWriteInterceptOnlyLogging() {
        MockRemoteCache c = (MockRemoteCache) MockRemoteCacheBuilder.createMockRemoteCacheBuilder()
                .keyPrefix("")
                .addWriteInterceptor(new LoggingExternalCacheWriteInterceptor())
                .buildCache();
        c.put("key", "value");
        c.putIfAbsent("key", "value");
        Map map = new LinkedHashMap();
        map.put("K1", "V1");
        map.put("K2", "V2");
        c.putAll(map);
        c.put("key", "LONG_TEXT_VALUE");
    }

    @Test
    public void testWriteInterceptWithBigKeyGuardAndLogging() {
        List<ExternalCacheWriteInterceptor> interceptors = new ArrayList<>();
        LoggingExternalCacheWriteInterceptor loggingExternalCacheWriteInterceptor = new LoggingExternalCacheWriteInterceptor();
        BigKeyDetectExternalCacheWriteInterceptor bigKeyDetectExternalCacheWriteInterceptor = new BigKeyDetectExternalCacheWriteInterceptor();
        interceptors.add(loggingExternalCacheWriteInterceptor);
        interceptors.add(bigKeyDetectExternalCacheWriteInterceptor);
        ExternalCacheBuilder builder = MockRemoteCacheBuilder.createMockRemoteCacheBuilder().keyPrefix("");
        builder.setWriteInterceptors(interceptors);
        MockRemoteCache c = (MockRemoteCache) builder.buildCache();
        c.put("key", "value");
        c.putIfAbsent("key", "value");
        Map map = new LinkedHashMap();
        map.put("K1", "V1");
        map.put("K2", "V2");
        c.putAll(map);
        assertEquals(0, bigKeyDetectExternalCacheWriteInterceptor.getBigKeyCount());
        c.put("key", "LONG_TEXT_VALUE");
        assertEquals(1, bigKeyDetectExternalCacheWriteInterceptor.getBigKeyCount());
    }

    @Test
    public void testWriteContextExposeSizesOnly() {
        SizeCaptureInterceptor interceptor = new SizeCaptureInterceptor();
        MockRemoteCache c = (MockRemoteCache) MockRemoteCacheBuilder.createMockRemoteCacheBuilder()
                .keyPrefix("")
                .addWriteInterceptor(interceptor)
                .buildCache();

        c.put("key", "value");

        assertEquals(ExternalCacheWriteInterceptor.WriteContext.Op.PUT, interceptor.getLastOp());
        assertTrue(interceptor.getLastKeySize() > 0);
        assertTrue(interceptor.getLastValueSize() > 0);
    }

    @Test
    public void testInterceptorExceptionShouldFailWriteWithoutThrowing() {
        MockRemoteCache c = (MockRemoteCache) MockRemoteCacheBuilder.createMockRemoteCacheBuilder()
                .keyPrefix("")
                .addWriteInterceptor(new FailingInterceptor())
                .buildCache();

        CacheResult result = c.PUT("key", "value");
        assertEquals(CacheResultCode.FAIL, result.getResultCode());
        assertNull(c.get("key"));

        c.put("key2", "value2");
        assertNull(c.get("key2"));

        assertFalse(c.putIfAbsent("key3", "value3"));
        assertNull(c.get("key3"));
    }

    @Test
    public void testPutAllShouldNotPartiallyWriteWhenInterceptorFailsMidBatch() {
        MockRemoteCache c = (MockRemoteCache) MockRemoteCacheBuilder.createMockRemoteCacheBuilder()
                .keyPrefix("")
                .addWriteInterceptor(new KeyRejectingInterceptor("K2"))
                .buildCache();

        Map<String, String> map = new LinkedHashMap<>();
        map.put("K1", "V1");
        map.put("K2", "V2");

        CacheResult result = c.PUT_ALL(map);
        assertEquals(CacheResultCode.FAIL, result.getResultCode());
        assertNull(c.get("K1"));
        assertNull(c.get("K2"));
    }

    private static class SizeCaptureInterceptor implements ExternalCacheWriteInterceptor {
        private int lastKeySize;
        private int lastValueSize;
        private WriteContext.Op lastOp;

        @Override
        public <K, V> void intercept(WriteContext<K, V> ctx) {
            lastKeySize = ctx.getKeySize();
            lastValueSize = ctx.getValueSize();
            lastOp = ctx.getOp();
        }

        int getLastKeySize() {
            return lastKeySize;
        }

        int getLastValueSize() {
            return lastValueSize;
        }

        WriteContext.Op getLastOp() {
            return lastOp;
        }
    }

    private static class FailingInterceptor implements ExternalCacheWriteInterceptor {
        @Override
        public <K, V> void intercept(WriteContext<K, V> ctx) {
            throw new IllegalStateException("interceptor failure");
        }
    }

    private static class KeyRejectingInterceptor implements ExternalCacheWriteInterceptor {
        private final Object rejectedKey;

        private KeyRejectingInterceptor(Object rejectedKey) {
            this.rejectedKey = rejectedKey;
        }

        @Override
        public <K, V> void intercept(WriteContext<K, V> ctx) {
            if (rejectedKey.equals(ctx.getKeyObj())) {
                throw new IllegalStateException("reject key " + rejectedKey);
            }
        }
    }


}
