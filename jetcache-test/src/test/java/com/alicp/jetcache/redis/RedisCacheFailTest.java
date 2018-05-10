/**
 * Created on 2018/5/10.
 */
package com.alicp.jetcache.redis;

import com.alicp.jetcache.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class RedisCacheFailTest {

    private Cache cache;
    private Jedis jedis;

    @BeforeEach
    public void setup() {
        JedisPool pool = mock(JedisPool.class);
        jedis = mock(Jedis.class);
        when(pool.getResource()).thenReturn(jedis);
        cache = RedisCacheBuilder.createRedisCacheBuilder()
                .keyPrefix("fail_test").jedisPool(pool).buildCache();
    }

    @Test
    public void test_GET() {
        when(jedis.get((byte[]) any())).thenThrow(new JedisConnectionException("err"));
        CacheGetResult cr = cache.GET("K");
        assertEquals(CacheResultCode.FAIL, cr.getResultCode());
        assertNull(cr.getValue());
    }

    @Test
    public void test_GET_ALL() {
        when(jedis.mget((byte[][]) any())).thenThrow(new JedisDataException("err"));
        HashSet s = new HashSet();
        s.add("K");
        MultiGetResult cr = cache.GET_ALL(s);
        assertEquals(CacheResultCode.FAIL, cr.getResultCode());
        assertNull(cr.getValues());
    }

    @Test
    public void test_PUT() {
        when(jedis.psetex((byte[]) any(), anyLong(), any()))
                .thenThrow(new JedisConnectionException("err"))
                .thenReturn("XXX");

        CacheResult cr = cache.PUT("K", "V");
        assertEquals(CacheResultCode.FAIL, cr.getResultCode());

        cr = cache.PUT("K", "V");
        assertEquals(CacheResultCode.FAIL, cr.getResultCode());
        assertEquals("XXX", cr.getMessage());
    }

    @Test
    public void test_PUT_ALL() {
        when(jedis.pipelined()).thenThrow(new JedisConnectionException("err"));
        Map m = new HashMap();
        m.put("K", "V");
        CacheResult cr = cache.PUT_ALL(m);
        assertEquals(CacheResultCode.FAIL, cr.getResultCode());
    }

    @Test
    public void test_REMOVE() {
        when(jedis.del((byte[])any())).thenThrow(new JedisConnectionException("err"));
        CacheResult cr = cache.REMOVE("K");
        assertEquals(CacheResultCode.FAIL, cr.getResultCode());
    }

    @Test
    public void test_REMOVE_ALL() {
        when(jedis.del((byte[][])any())).thenThrow(new JedisConnectionException("err"));
        HashSet s = new HashSet();
        s.add("K");
        CacheResult cr = cache.REMOVE_ALL(s);
        assertEquals(CacheResultCode.FAIL, cr.getResultCode());
    }

    @Test
    public void test_PUT_IF_ABSENT() {
        when(jedis.set((byte[])any(), any(), any(), any(), anyLong()))
                .thenThrow(new JedisConnectionException("err"))
                .thenReturn("XXX");
        CacheResult cr = cache.PUT_IF_ABSENT("K", "V", 1, TimeUnit.SECONDS);
        assertEquals(CacheResultCode.FAIL, cr.getResultCode());

        cr = cache.PUT_IF_ABSENT("K", "V", 1, TimeUnit.SECONDS);
        assertEquals(CacheResultCode.FAIL, cr.getResultCode());
        assertEquals("XXX", cr.getMessage());
    }
}
