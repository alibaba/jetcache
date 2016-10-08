/**
 * Created on  13-10-21 09:47
 */
package com.alicp.jetcache.embedded;

import com.alicp.jetcache.AbstractCacheTest;
import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheConfig;
import com.alicp.jetcache.CacheResultCode;
import com.alicp.jetcache.support.FastjsonKeyConvertor;
import org.junit.Assert;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public abstract class AbstractEmbeddedCacheTest extends AbstractCacheTest {

    protected abstract Function<CacheConfig, Cache> getBuildFunc();

    private void lruTest() {
        Assert.assertEquals(CacheResultCode.SUCCESS, cache.PUT("K1", "V1", 1, TimeUnit.SECONDS).getResultCode());
        Assert.assertEquals(CacheResultCode.SUCCESS, cache.PUT("K2", "V2", 1, TimeUnit.SECONDS).getResultCode());
        Assert.assertEquals(CacheResultCode.SUCCESS, cache.PUT("K3", "V3", 1, TimeUnit.SECONDS).getResultCode());
        Assert.assertEquals(CacheResultCode.NOT_EXISTS, cache.GET("K1").getResultCode());
        Assert.assertEquals(CacheResultCode.SUCCESS, cache.GET("K2").getResultCode());
        Assert.assertEquals(CacheResultCode.SUCCESS, cache.GET("K3").getResultCode());

        Assert.assertEquals(CacheResultCode.SUCCESS, cache.PUT("K1", "V1", 1, TimeUnit.SECONDS).getResultCode());
        Assert.assertEquals(CacheResultCode.SUCCESS, cache.PUT("K2", "V2", 1, TimeUnit.SECONDS).getResultCode());
        Assert.assertEquals(CacheResultCode.SUCCESS, cache.GET("K1").getResultCode());
        Assert.assertEquals(CacheResultCode.SUCCESS, cache.PUT("K3", "V3", 1, TimeUnit.SECONDS).getResultCode());
        Assert.assertEquals(CacheResultCode.SUCCESS, cache.GET("K1").getResultCode());
        Assert.assertEquals(CacheResultCode.NOT_EXISTS, cache.GET("K2").getResultCode());
        Assert.assertEquals(CacheResultCode.SUCCESS, cache.GET("K3").getResultCode());
    }

    public void test() throws Exception {
        cache = EmbeddedCacheBuilder.createEmbeddedCacheBuilder()
                .buildFunc(getBuildFunc()).defaultExpire(100, TimeUnit.MILLISECONDS).limit(2).build();
        baseTest();
        expireAfterWriteTest();
        lruTest();

        cache = EmbeddedCacheBuilder.createEmbeddedCacheBuilder()
                .buildFunc(getBuildFunc()).expireAfterAccess().defaultExpire(100, TimeUnit.MILLISECONDS).limit(2).build();
        baseTest();
        expireAfterAccessTest();
        lruTest();

        cache = EmbeddedCacheBuilder.createEmbeddedCacheBuilder()
                .buildFunc(getBuildFunc()).softValues().defaultExpire(100, TimeUnit.MILLISECONDS).limit(2).build();
        baseTest();
        expireAfterWriteTest();
        lruTest();

        cache = EmbeddedCacheBuilder.createEmbeddedCacheBuilder()
                .buildFunc(getBuildFunc()).weakValues().defaultExpire(100, TimeUnit.MILLISECONDS).limit(2).build();
        baseTest();
        expireAfterWriteTest();
        lruTest();

        cache = EmbeddedCacheBuilder.createEmbeddedCacheBuilder().buildFunc(getBuildFunc()).keyConvertor(FastjsonKeyConvertor.INSTANCE).build();
        testKeyCoverter();

        int thread = 10;
        int count = 100;
        int time = 1000;
        cache = EmbeddedCacheBuilder.createEmbeddedCacheBuilder().buildFunc(getBuildFunc()).limit(thread * count).build();
        testConcurrentImpl(thread, count ,time);
    }

}
