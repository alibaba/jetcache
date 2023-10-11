/**
 * Created on 2023/01/05.
 */
package com.alicp.jetcache.external;

import com.alicp.jetcache.RefreshCache;
import com.alicp.jetcache.anno.KeyConvertor;
import com.alicp.jetcache.support.Fastjson2KeyConvertor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * @author huangli
 */
public class ExternalCacheBuildKeyTest {
    @Test
    public void testBuildKey() {
        MockRemoteCache c = (MockRemoteCache) MockRemoteCacheBuilder.createMockRemoteCacheBuilder()
                .keyPrefix("")
                .buildCache();
        byte[] byteKey = new byte[]{1, 2, 3};
        String strKey = "123";
        assertArrayEquals(byteKey, c.buildKey(byteKey));
        assertArrayEquals(strKey.getBytes(), c.buildKey(strKey));

        c.config().setKeyConvertor(Fastjson2KeyConvertor.INSTANCE);
        assertArrayEquals(byteKey, c.buildKey(byteKey));
        assertArrayEquals(strKey.getBytes(), c.buildKey(strKey));

        String convertedKey = "456";
        c.config().setKeyConvertor((KeyConvertor) o -> convertedKey);
        assertArrayEquals(convertedKey.getBytes(), c.buildKey(byteKey));
        assertArrayEquals(convertedKey.getBytes(), c.buildKey(strKey));
        assertArrayEquals(convertedKey.getBytes(), c.buildKey("long long long str"));
        assertArrayEquals(convertedKey.getBytes(), c.buildKey(1));

        strKey = "123" + new String(RefreshCache.LOCK_KEY_SUFFIX);
        assertArrayEquals(strKey.getBytes(), c.buildKey(strKey.getBytes()));
        strKey = "123" + new String(RefreshCache.TIMESTAMP_KEY_SUFFIX);
        assertArrayEquals(strKey.getBytes(), c.buildKey(strKey.getBytes()));
        strKey = "" + new String(RefreshCache.TIMESTAMP_KEY_SUFFIX);
        assertArrayEquals(strKey.getBytes(), c.buildKey(strKey.getBytes()));
    }
}
