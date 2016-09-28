/**
 * Created on  13-10-21 09:47
 */
package com.alicp.jetcache.embedded;

import com.alicp.jetcache.cache.Cache;
import com.alicp.jetcache.cache.CacheConfig;
import com.alicp.jetcache.cache.CacheGetResult;
import com.alicp.jetcache.cache.CacheResultCode;
import org.junit.Assert;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public abstract class AbstractLocalCacheTest {
    protected Cache<String,String> cache;

    protected abstract Function<CacheConfig, Cache> getBuildFunc();

    protected void setup(boolean useSofeRef, int limit){
        cache = EmbeddedCacheBuilder.createLocalCacheBuilder().withLimit(limit)
                .withUseSoftRef(useSofeRef).withBuildFunc(getBuildFunc()).build();
    }

    public void test1() throws Exception {
        for (int i = 0; i < 2; i++) {
            setup(i == 0, 100);
            Assert.assertEquals(CacheResultCode.NOT_EXISTS, cache.GET("K1").getResultCode());
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.PUT("K1", "V1", 1, TimeUnit.SECONDS));
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.GET("K1").getResultCode());
            Assert.assertEquals("V1", cache.GET("K1").getValue());
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.PUT("K1", "V2", 1, TimeUnit.SECONDS));
            Assert.assertEquals("V2", cache.GET("K1").getValue());
        }
    }

    public void testLRU1() {
        for (int i = 0; i < 2; i++) {
            setup(i == 0, 2);
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.PUT("K1", "V1", 1, TimeUnit.SECONDS));
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.PUT("K2", "V2", 1, TimeUnit.SECONDS));
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.PUT("K3", "V3", 1, TimeUnit.SECONDS));
            Assert.assertEquals(CacheResultCode.NOT_EXISTS, cache.GET("K1").getResultCode());
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.GET("K2").getResultCode());
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.GET("K3").getResultCode());
        }
    }

    public void testLRU2() {
        for (int i = 0; i < 2; i++) {
            setup(i == 0, 2);
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.PUT("K1", "V1", 1, TimeUnit.SECONDS));
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.PUT("K2", "V2", 1, TimeUnit.SECONDS));
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.GET("K1").getResultCode());
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.PUT("K3", "V3", 1, TimeUnit.SECONDS));
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.GET("K1").getResultCode());
            Assert.assertEquals(CacheResultCode.NOT_EXISTS, cache.GET("K2").getResultCode());
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.GET("K3").getResultCode());
        }
    }

    public void testExpire() throws Exception {
        for (int i = 0; i < 2; i++) {
            setup(i == 0, 100);
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.PUT("K1", "V1", 1, TimeUnit.SECONDS));

            CacheGetResult result = cache.GET("K1");
            Assert.assertEquals(CacheResultCode.SUCCESS, result.getResultCode());
            Assert.assertEquals("V1", result.getValue());

            Thread.sleep(1001);
            result = cache.GET("K1");
            Assert.assertEquals(CacheResultCode.EXPIRED, result.getResultCode());
            Assert.assertNull(result.getValue());
        }
    }

    public void testNull() {
        for (int i = 0; i < 2; i++) {
            setup(i == 0, 100);
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.PUT("K1", null, 1, TimeUnit.SECONDS));
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.GET("K1").getResultCode());
            Assert.assertNull(cache.GET("K1").getValue());
        }
    }

    public void testConcurrent() throws Exception {
        testConcurrentImpl(10, 100);
    }

    private volatile boolean cocurrentFail = false;

    private void testConcurrentImpl(int threadCount, int count) throws Exception {
        setup(false, threadCount * count);
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
