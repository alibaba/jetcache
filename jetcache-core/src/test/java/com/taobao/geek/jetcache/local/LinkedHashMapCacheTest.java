/**
 * Created on  13-09-24 10:20
 */
package com.taobao.geek.jetcache.local;

import com.taobao.geek.jetcache.support.Cache;
import com.taobao.geek.jetcache.support.CacheConfig;
import com.taobao.geek.jetcache.support.CacheResult;
import com.taobao.geek.jetcache.support.CacheResultCode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class LinkedHashMapCacheTest {
    private Cache cache;
    private CacheConfig cc;

    protected void setup(boolean useSofeRef) {
        cache = new LinkedHashMapCache(useSofeRef);
        cc = new CacheConfig();
    }

    @Test
    public void test1() throws Exception {
        for (int i = 0; i < 2; i++) {
            setup(i == 0);
            Assert.assertEquals(CacheResultCode.NOT_EXISTS, cache.get(cc, "S1", "K1").getResultCode());
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.put(cc, "S1", "K1", "V1"));
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.get(cc, "S1", "K1").getResultCode());
            Assert.assertEquals("V1", cache.get(cc, "S1", "K1").getValue());
            Assert.assertEquals(CacheResultCode.NOT_EXISTS, cache.get(cc, "S2", "K1").getResultCode());
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.put(cc, "S1", "K1", "V2"));
            Assert.assertEquals("V2", cache.get(cc, "S1", "K1").getValue());

            cc.setArea("A2");
            cc.setExpire(30 * 24 * 3600);
            Assert.assertEquals(CacheResultCode.NOT_EXISTS, cache.get(cc, "S1", "K1").getResultCode());
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.put(cc, "S1", "K1", "V1"));
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.get(cc, "S1", "K1").getResultCode());
            Assert.assertEquals("V1", cache.get(cc, "S1", "K1").getValue());
        }
    }

    private void test1Impl() {

    }

    @Test
    public void testLRU1() {
        for (int i = 0; i < 2; i++) {
            setup(i == 0);
            cc.setLocalLimit(2);
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.put(cc, "S1", "K1", "V1"));
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.put(cc, "S1", "K2", "V2"));
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.put(cc, "S1", "K3", "V3"));
            Assert.assertEquals(CacheResultCode.NOT_EXISTS, cache.get(cc, "S1", "K1").getResultCode());
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.get(cc, "S1", "K2").getResultCode());
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.get(cc, "S1", "K3").getResultCode());
        }
    }

    @Test
    public void testLRU2() {
        for (int i = 0; i < 2; i++) {
            setup(i == 0);
            cc.setLocalLimit(2);
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.put(cc, "S1", "K1", "V1"));
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.put(cc, "S1", "K2", "V2"));
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.get(cc, "S1", "K1").getResultCode());
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.put(cc, "S1", "K3", "V3"));
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.get(cc, "S1", "K1").getResultCode());
            Assert.assertEquals(CacheResultCode.NOT_EXISTS, cache.get(cc, "S1", "K2").getResultCode());
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.get(cc, "S1", "K3").getResultCode());
        }
    }

    @Test
    public void testLRU3() throws Exception {
        for (int i = 0; i < 2; i++) {
            setup(i == 0);
            cc.setExpire(1);
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.put(cc, "S1", "K1", "V1"));
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.get(cc, "S1", "K1").getResultCode());
            Thread.sleep(1001);
            Assert.assertEquals(CacheResultCode.EXPIRED, cache.get(cc, "S1", "K1").getResultCode());
        }
    }

    @Test
    public void testNull() {
        for (int i = 0; i < 2; i++) {
            setup(i == 0);
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.put(cc, "S1", "K1", null));
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.get(cc, "S1", "K1").getResultCode());
            Assert.assertNull(cache.get(cc, "S1", "K1").getValue());
        }
    }

    @Test
    public void testConcurrent() throws Exception {
        setup(false);
        testConcurrentImpl();
    }

    private volatile boolean cocurrentFail = false;

    private void testConcurrentImpl() throws Exception {
        class T extends Thread {
            String keyPrefix;
            private String subArea;
            transient boolean stop;

            T(String keyPrefix, String subArea) {
                this.keyPrefix = keyPrefix;
                this.subArea = subArea;
            }

            @Override
            public void run() {
                try {
                    int i = 0;
                    while (!stop) {
                        i++;
                        if (i >= 1000) {
                            i = 0;
                        }
                        String key = keyPrefix + i;
                        String value = i + "";
                        cache.put(cc, subArea, key, value);
                        CacheResult result = cache.get(cc, subArea, key);
                        if (result == null || result.getResultCode() != CacheResultCode.SUCCESS) {
                            if (result == null) {
                                System.out.println("subArea:" + subArea + ",key:" + key + ",result is null");
                            } else {
                                System.out.println("subArea:" + subArea + ",key:" + key + ",code:" + result.getResultCode());
                            }
                            cocurrentFail = true;
                        } else if (!result.getValue().equals(value)) {
                            System.out.println("subArea:" + subArea + ",key:" + key + ",value:" + result.getValue());
                            cocurrentFail = true;
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    cocurrentFail = true;
                }

            }
        }

        int threadCount = 10;
        cc.setLocalLimit(threadCount * 1000 / 2);

        T[] t = new T[threadCount];
        for (int i = 0; i < threadCount; i++) {
            t[i] = new T("T" + i + "_", "S" + (i % 2));
            t[i].setName("ConTest" + i);
            t[i].start();
        }

        Thread.sleep(10000);
        for (int i = 0; i < threadCount; i++) {
            t[i].stop = true;
        }

        if (cocurrentFail) {
            Assert.fail();
        }
    }
}
