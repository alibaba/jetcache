/**
 * Created on  13-10-21 09:47
 */
package com.alicp.jetcache.embedded;

import com.alicp.jetcache.*;
import com.alicp.jetcache.anno.support.FastjsonKeyGenerator;
import com.alicp.jetcache.testsupport.DynamicQuery;
import org.junit.Assert;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public abstract class AbstractEmbeddedCacheTest {
    protected Cache<Object, Object> cache;
    protected EmbeddedCacheBuilder.EmbeddedCacheBuilderImpl builder;

    protected abstract Function<CacheConfig, Cache> getBuildFunc();

    private void baseTest() {
        Assert.assertEquals(CacheResultCode.NOT_EXISTS, cache.GET("BASE_K1").getResultCode());
        Assert.assertEquals(CacheResultCode.SUCCESS, cache.PUT("BASE_K1", "V1", 1, TimeUnit.SECONDS).getResultCode());
        Assert.assertEquals(CacheResultCode.SUCCESS, cache.GET("BASE_K1").getResultCode());
        Assert.assertEquals("V1", cache.GET("BASE_K1").getValue());
        Assert.assertEquals(CacheResultCode.SUCCESS, cache.PUT("BASE_K1", "V2", 1, TimeUnit.SECONDS).getResultCode());
        Assert.assertEquals("V2", cache.GET("BASE_K1").getValue());

        cache.put("BASE_K2", null);
        CacheGetResult<Object> r = cache.GET("BASE_K2");
        Assert.assertTrue(r.isSuccess());
        Assert.assertNull(r.getValue());
    }

    private void expireTest() throws Exception {
        EmbeddedCacheConfig config = builder.getConfig();
        long defaultExpireInMillis = config.getDefaultExpireInMillis();
        if (!config.isExpireAfterAccess()) {
            long ttl = defaultExpireInMillis;
            cache.put("EXPIRE_K1", "V1");
            expireTestImpl1(ttl);

            ttl = ttl / 2;
            cache.put("EXPIRE_K1", "V1", ttl, TimeUnit.MILLISECONDS);
            expireTestImpl1(ttl);
        } else {
            long ttl = defaultExpireInMillis;
            cache.put("EXPIRE_K1", "V1");
            expireTestImpl2(ttl);

            ttl = ttl / 2;
            cache.put("EXPIRE_K1", "V1", ttl, TimeUnit.MILLISECONDS);
            expireTestImpl2(ttl);
        }
    }

    private void expireTestImpl1(long ttl) throws InterruptedException {
        Assert.assertEquals("V1", cache.get("EXPIRE_K1"));
        Thread.sleep(ttl / 2);
        Assert.assertEquals("V1", cache.get("EXPIRE_K1"));
        Thread.sleep(ttl / 2 + 1);
        CacheGetResult<Object> r = cache.GET("EXPIRE_K1");
        Assert.assertEquals(CacheResultCode.EXPIRED, r.getResultCode());
        Assert.assertNull(r.getValue());
    }

    private void expireTestImpl2(long ttl) throws InterruptedException {
        Assert.assertEquals("V1", cache.get("EXPIRE_K1"));
        Thread.sleep(ttl / 2);
        Assert.assertEquals("V1", cache.get("EXPIRE_K1"));
        Thread.sleep(ttl / 2 + 1);
        Assert.assertEquals("V1", cache.get("EXPIRE_K1"));
        Thread.sleep(ttl + 1);
        CacheGetResult<Object> r = cache.GET("EXPIRE_K1");
        Assert.assertEquals(CacheResultCode.EXPIRED, r.getResultCode());
        Assert.assertNull(r.getValue());
    }

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
        builder = EmbeddedCacheBuilder.createEmbeddedCacheBuilder();
        cache = builder.buildFunc(getBuildFunc()).defaultExpire(TimeUnit.MILLISECONDS, 100).limit(2).build();
        baseTest();
        expireTest();
        lruTest();

        builder = EmbeddedCacheBuilder.createEmbeddedCacheBuilder();
        cache = builder.buildFunc(getBuildFunc()).softValues().defaultExpire(TimeUnit.MILLISECONDS, 100).limit(2).build();
        baseTest();
        expireTest();
        lruTest();

        builder = EmbeddedCacheBuilder.createEmbeddedCacheBuilder();
        cache = builder.buildFunc(getBuildFunc()).weakValues().defaultExpire(TimeUnit.MILLISECONDS, 100).limit(2).build();
        baseTest();
        expireTest();
        lruTest();

        testKeyCoverter();
    }

    public void testKeyCoverter() {
        DynamicQuery d1 = new DynamicQuery();
        DynamicQuery d2 = new DynamicQuery();
        DynamicQuery d3 = new DynamicQuery();
        d1.setId(100);
        d2.setId(100);
        d3.setId(101);
        d1.setName("HL");
        d2.setName("HL");

        builder = EmbeddedCacheBuilder.createEmbeddedCacheBuilder();
        cache = builder.buildFunc(getBuildFunc()).build();
        cache.put(d1, "V1");
        Assert.assertNull(cache.get(d2));

        builder = EmbeddedCacheBuilder.createEmbeddedCacheBuilder();
        cache = builder.buildFunc(getBuildFunc()).keyConvertor(FastjsonKeyGenerator.INSTANCE).build();
        cache.put(d1, "V1");
        Assert.assertEquals("V1", cache.get(d2));
        Assert.assertNull(cache.get(d3));
    }

    public void testConcurrent() throws Exception {
        testConcurrentImpl(10, 100);
    }

    private volatile boolean cocurrentFail = false;

    private void testConcurrentImpl(int threadCount, int count) throws Exception {
        builder = EmbeddedCacheBuilder.createEmbeddedCacheBuilder();
        cache = builder.buildFunc(getBuildFunc()).limit(threadCount * count).build();
        class T extends Thread {
            private String keyPrefix;
            private transient boolean stop;

            private T(String keyPrefix) {
                this.keyPrefix = keyPrefix;
            }

            @Override
            public void run() {
                try {
                    int i = 0;
                    while (!stop) {
                        i++;
                        if (i >= count) {
                            i = 0;
                        }
                        String key = keyPrefix + i;
                        String value = i + "";
                        cache.PUT(key, value, 10000, TimeUnit.SECONDS);
                        CacheGetResult result = cache.GET(key);
                        if (result == null || result.getResultCode() != CacheResultCode.SUCCESS) {
                            if (result == null) {
                                System.out.println("key:" + key + ",result is null");
                            } else {
                                System.out.println("key:" + key + ",code:" + result.getResultCode());
                            }
                            cocurrentFail = true;
                        } else if (!result.getValue().equals(value)) {
                            System.out.println("key:" + key + ",value:" + result.getValue());
                            cocurrentFail = true;
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    cocurrentFail = true;
                }

            }
        }

        T[] t = new T[threadCount];
        for (int i = 0; i < threadCount; i++) {
            t[i] = new T("T" + i + "_");
            t[i].setName("ConTest" + i);
            t[i].start();
        }

        Thread.sleep(1000);
        for (int i = 0; i < threadCount; i++) {
            t[i].stop = true;
        }

        if (cocurrentFail) {
            Assert.fail();
        }
    }
}
