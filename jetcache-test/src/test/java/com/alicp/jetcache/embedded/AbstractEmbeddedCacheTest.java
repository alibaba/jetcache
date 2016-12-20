/**
 * Created on  13-10-21 09:47
 */
package com.alicp.jetcache.embedded;

import com.alicp.jetcache.test.AbstractCacheTest;
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

    public void test(int expireMillis, boolean testLru) throws Exception {
        cache = EmbeddedCacheBuilder.createEmbeddedCacheBuilder()
                .buildFunc(getBuildFunc()).expireAfterWrite(expireMillis, TimeUnit.MILLISECONDS).limit(200).buildCache();
        baseTest();
        expireAfterWriteTest(cache.config().getDefaultExpireInMillis());
        if(testLru) {
            cache = EmbeddedCacheBuilder.createEmbeddedCacheBuilder()
                    .buildFunc(getBuildFunc()).expireAfterWrite(expireMillis, TimeUnit.MILLISECONDS).limit(2).buildCache();
            lruTest();
        }

        cache = EmbeddedCacheBuilder.createEmbeddedCacheBuilder()
                .buildFunc(getBuildFunc()).expireAfterAccess(expireMillis, TimeUnit.MILLISECONDS).limit(200).buildCache();
        baseTest();
        expireAfterAccessTest(cache.config().getDefaultExpireInMillis());
        if(testLru) {
            cache = EmbeddedCacheBuilder.createEmbeddedCacheBuilder()
                    .buildFunc(getBuildFunc()).expireAfterAccess(expireMillis, TimeUnit.MILLISECONDS).limit(2).buildCache();
            lruTest();
        }

        cache = EmbeddedCacheBuilder.createEmbeddedCacheBuilder().buildFunc(getBuildFunc()).keyConvertor(FastjsonKeyConvertor.INSTANCE).buildCache();
        keyCoverterTest();

        int thread = 10;
        int count = 100;
        int time = 5000;
        cache = EmbeddedCacheBuilder.createEmbeddedCacheBuilder().buildFunc(getBuildFunc()).limit(thread * count).buildCache();
        concurrentTest(thread, count ,time);
    }

}
