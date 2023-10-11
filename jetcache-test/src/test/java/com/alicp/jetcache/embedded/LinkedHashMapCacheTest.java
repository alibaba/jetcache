/**
 * Created on  13-09-24 10:20
 */
package com.alicp.jetcache.embedded;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheConfig;
import com.alicp.jetcache.CacheResultCode;
import com.alicp.jetcache.VirtualThreadUtil;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.cglib.core.ReflectUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author huangli
 */
public class LinkedHashMapCacheTest extends AbstractEmbeddedCacheTest {

    @Override
    protected Function<CacheConfig, Cache> getBuildFunc() {
        return (c) -> new LinkedHashMapCache((EmbeddedCacheConfig) c);
    }

    @Test
    public void test() throws Exception {
        super.test(100, true);
    }

    @Test
    public void mutilTest() throws InterruptedException{
        cache = EmbeddedCacheBuilder.createEmbeddedCacheBuilder()
                .buildFunc(getBuildFunc()).expireAfterWrite(5000, TimeUnit.MILLISECONDS).limit(10240).buildCache();
        ExecutorService executorService = VirtualThreadUtil.createExecuteor();
        if(executorService == null){
            executorService = Executors.newFixedThreadPool(3);
        }

        executorService.submit(() -> {
            for (int i = 0; i < 1000; i+=2) {
                cache.putIfAbsent("K" + i, "V" + i);
            }
        });
        executorService.submit(() -> {
            for (int i = 1; i < 1000; i+=2) {
                cache.remove("K" + i);
            }
        });
        executorService.submit(() -> {
            for (int i = 0; i < 1000; i++) {
                Object value = cache.get("K" + i);
                if(!Objects.isNull(value))
                    Assert.assertEquals(("V"+i),value);
            }
        });
        executorService.shutdown();
        executorService.awaitTermination(10,TimeUnit.SECONDS);
    }

    @Test
    public void cleanTest() throws Exception {
        cache = EmbeddedCacheBuilder.createEmbeddedCacheBuilder()
                .buildFunc(getBuildFunc()).expireAfterWrite(2000, TimeUnit.MILLISECONDS).limit(3).buildCache();
        cache.put("K1", "V1", 1, TimeUnit.MILLISECONDS);
        Thread.sleep(1);
        Assert.assertEquals(CacheResultCode.EXPIRED, cache.GET("K1").getResultCode());
        ((LinkedHashMapCache) cache).cleanExpiredEntry();
        Assert.assertEquals(CacheResultCode.NOT_EXISTS, cache.GET("K1").getResultCode());
    }


}
