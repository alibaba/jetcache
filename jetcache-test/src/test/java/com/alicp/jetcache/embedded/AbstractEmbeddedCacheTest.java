/**
 * Created on  13-10-21 09:47
 */
package com.alicp.jetcache.embedded;

import com.alicp.jetcache.*;
import com.alicp.jetcache.support.DefaultCacheMonitorTest;
import com.alicp.jetcache.support.FastjsonKeyConvertor;
import com.alicp.jetcache.test.AbstractCacheTest;
import com.alicp.jetcache.test.support.DynamicQuery;
import com.alicp.jetcache.test.support.DynamicQueryWithEquals;
import org.junit.Assert;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
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
        expireAfterWriteTest(cache.config().getExpireAfterWriteInMillis());
        if (testLru) {
            cache = EmbeddedCacheBuilder.createEmbeddedCacheBuilder()
                    .buildFunc(getBuildFunc()).expireAfterWrite(expireMillis, TimeUnit.MILLISECONDS).limit(2).buildCache();
            lruTest();
        }

        cache = EmbeddedCacheBuilder.createEmbeddedCacheBuilder()
                .buildFunc(getBuildFunc()).expireAfterAccess(expireMillis, TimeUnit.MILLISECONDS).limit(200).buildCache();
        baseTest();
        expireAfterAccessTest(cache.config().getExpireAfterAccessInMillis());
        if (testLru) {
            cache = EmbeddedCacheBuilder.createEmbeddedCacheBuilder()
                    .buildFunc(getBuildFunc()).expireAfterAccess(expireMillis, TimeUnit.MILLISECONDS).limit(2).buildCache();
            lruTest();
        }

        cache = EmbeddedCacheBuilder.createEmbeddedCacheBuilder().buildFunc(getBuildFunc())
                .keyConvertor(FastjsonKeyConvertor.INSTANCE).buildCache();
        fastjsonKeyCoverterTest();

        cache = EmbeddedCacheBuilder.createEmbeddedCacheBuilder().buildFunc(getBuildFunc())
                .keyConvertor(null).buildCache();
        nullKeyConvertorTest();

        cache = EmbeddedCacheBuilder.createEmbeddedCacheBuilder().buildFunc(getBuildFunc()).buildCache();
        DefaultCacheMonitorTest.testMonitor(cache);

        LoadingCacheTest.loadingCacheTest(EmbeddedCacheBuilder.createEmbeddedCacheBuilder().buildFunc(getBuildFunc()), 0);

        RefreshCacheTest.refreshCacheTest(EmbeddedCacheBuilder.createEmbeddedCacheBuilder().buildFunc(getBuildFunc()), 80, 40);


        int thread = 10;
        int limit = 1000;
        int time = 3000;
        cache = EmbeddedCacheBuilder.createEmbeddedCacheBuilder().buildFunc(getBuildFunc()).limit(limit).buildCache();
        concurrentTest(thread, limit, time);
    }

    protected void nullKeyConvertorTest() {
        {
            DynamicQuery d1 = new DynamicQuery();
            DynamicQuery d2 = new DynamicQuery();
            DynamicQuery d3 = new DynamicQuery();
            d1.setId(100);
            d2.setId(100);
            d3.setId(101);
            d1.setName("HL");
            d2.setName("HL");

            cache.put(d1, "V1");
            Assert.assertNull(cache.get(d2));
            Assert.assertNull(cache.get(d3));
        }

        {
            DynamicQueryWithEquals d1 = new DynamicQueryWithEquals();
            DynamicQueryWithEquals d2 = new DynamicQueryWithEquals();
            DynamicQueryWithEquals d3 = new DynamicQueryWithEquals();
            d1.setId(100);
            d2.setId(100);
            d3.setId(101);
            d1.setName("HL");
            d2.setName("HL2");

            cache.put(d1, "V2");
            Assert.assertEquals("V2", cache.get(d2));
            Assert.assertNull(cache.get(d3));
        }
    }

}
