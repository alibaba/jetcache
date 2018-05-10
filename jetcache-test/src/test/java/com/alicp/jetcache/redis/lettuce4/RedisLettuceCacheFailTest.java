/**
 * Created on 2018/5/10.
 */
package com.alicp.jetcache.redis.lettuce4;

import com.alicp.jetcache.*;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisFuture;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.api.async.RedisAsyncCommands;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class RedisLettuceCacheFailTest {

    private RedisClient client;
    private RedisAsyncCommands asyncCommands;
    private Cache cache;

    @BeforeEach
    public void setup() {
        client = mock(RedisClient.class);
        StatefulRedisConnection connection = mock(StatefulRedisConnection.class);
        asyncCommands = mock(RedisAsyncCommands.class);
        when(client.connect((JetCacheCodec) any())).thenReturn(connection);
        when(connection.sync()).thenReturn(null);
        when(connection.async()).thenReturn(asyncCommands);

        cache = RedisLettuceCacheBuilder.createRedisLettuceCacheBuilder()
                .redisClient(client)
                .keyPrefix("fail_test")
                .buildCache();
    }

    @AfterEach
    public void teardown() {
        LettuceConnectionManager.defaultManager().removeAndClose(client);
    }

    private RedisFuture mockFuture(Object value, Throwable ex) {
        RedisFuture redisFuture = mock(RedisFuture.class);
        when(redisFuture.handle(any())).thenAnswer((invoke) -> {
            BiFunction function = invoke.getArgument(0);
            Object resultData = function.apply(value, ex);
            return CompletableFuture.completedFuture(resultData);
        });
        return redisFuture;
    }

    @Test
    public void test_GET() {
        when(asyncCommands.get(any())).thenThrow(new RuntimeException("err"))
                .thenReturn(mockFuture(null, new RuntimeException()));
        CacheGetResult cr = cache.GET("K");
        assertEquals(CacheResultCode.FAIL, cr.getResultCode());
        assertNull(cr.getValue());

        cr = cache.GET("K");
        assertEquals(CacheResultCode.FAIL, cr.getResultCode());
        assertNull(cr.getValue());
    }

    @Test
    public void test_GET_ALL() {
        when(asyncCommands.mget(any())).thenThrow(new RuntimeException("err"))
                .thenReturn(mockFuture(null, new RuntimeException()));
        HashSet s = new HashSet();
        s.add("K");

        MultiGetResult cr = cache.GET_ALL(s);
        assertEquals(CacheResultCode.FAIL, cr.getResultCode());
        assertNull(cr.getValues());

        cr = cache.GET_ALL(s);
        assertEquals(CacheResultCode.FAIL, cr.getResultCode());
        assertNull(cr.getValues());
    }

    @Test
    public void test_PUT() {
        when(asyncCommands.psetex(any(), anyLong(), any()))
                .thenThrow(new RuntimeException("err"))
                .thenReturn(mockFuture(null, new RuntimeException()))
                .thenReturn(mockFuture("XXX", null));

        CacheResult cr = cache.PUT("K", "V");
        assertEquals(CacheResultCode.FAIL, cr.getResultCode());

        cr = cache.PUT("K", "V");
        assertEquals(CacheResultCode.FAIL, cr.getResultCode());

        cr = cache.PUT("K", "V");
        assertEquals(CacheResultCode.FAIL, cr.getResultCode());
        assertEquals("XXX", cr.getMessage());
    }

    @Test
    public void test_PUT_ALL() {
        when(asyncCommands.psetex(any(), anyLong(), any()))
                .thenThrow(new RuntimeException("err"));
        Map m = new HashMap();
        m.put("K", "V");
        CacheResult cr = cache.PUT_ALL(m);
        assertEquals(CacheResultCode.FAIL, cr.getResultCode());
    }

    @Test
    public void test_REMOVE() {
        when(asyncCommands.del((byte[]) any()))
                .thenThrow(new RuntimeException("err"))
                .thenReturn(mockFuture(null, new RuntimeException()))
                .thenReturn(mockFuture(null, null))
                .thenReturn(mockFuture(1000L, null));
        CacheResult cr = cache.REMOVE("K");
        assertEquals(CacheResultCode.FAIL, cr.getResultCode());

        cr = cache.REMOVE("K");
        assertEquals(CacheResultCode.FAIL, cr.getResultCode());

        cr = cache.REMOVE("K");
        assertEquals(CacheResultCode.FAIL, cr.getResultCode());

        cr = cache.REMOVE("K");
        assertEquals(CacheResultCode.FAIL, cr.getResultCode());
    }

    @Test
    public void test_REMOVE_ALL() {
        when(asyncCommands.del((byte[][]) any()))
                .thenThrow(new RuntimeException("err"))
                .thenReturn(mockFuture(null, new RuntimeException()));
        HashSet s = new HashSet();
        s.add("K");
        CacheResult cr = cache.REMOVE_ALL(s);
        assertEquals(CacheResultCode.FAIL, cr.getResultCode());

        cr = cache.REMOVE_ALL(s);
        assertEquals(CacheResultCode.FAIL, cr.getResultCode());
    }

    @Test
    public void test_PUT_IF_ABSENT() {
        when(asyncCommands.set(any(), any(), any()))
                .thenThrow(new RuntimeException("err"))
                .thenReturn(mockFuture(null, new RuntimeException()))
                .thenReturn(mockFuture("XXX", null));
        CacheResult cr = cache.PUT_IF_ABSENT("K", "V", 1, TimeUnit.SECONDS);
        assertEquals(CacheResultCode.FAIL, cr.getResultCode());

        cr = cache.PUT_IF_ABSENT("K", "V", 1, TimeUnit.SECONDS);
        assertEquals(CacheResultCode.FAIL, cr.getResultCode());

        cr = cache.PUT_IF_ABSENT("K", "V", 1, TimeUnit.SECONDS);
        assertEquals(CacheResultCode.FAIL, cr.getResultCode());
        assertEquals("XXX", cr.getMessage());
    }
}
