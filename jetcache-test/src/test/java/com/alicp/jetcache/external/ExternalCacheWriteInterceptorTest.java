
package com.alicp.jetcache.external;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

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
        Map map = new HashMap();
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
        Map map = new HashMap();
        map.put("K1", "V1");
        map.put("K2", "V2");
        c.putAll(map);
        assertEquals(0, bigKeyDetectExternalCacheWriteInterceptor.getBigKeyCount());
        c.put("key", "LONG_TEXT_VALUE");
        assertEquals(1, bigKeyDetectExternalCacheWriteInterceptor.getBigKeyCount());
    }


}
