/**
 * Created on  13-10-21 09:47
 */
package com.alicp.jetcache.local;

import com.alicp.jetcache.CacheConsts;
import com.alicp.jetcache.support.Cache;
import com.alicp.jetcache.support.CacheConfig;
import com.alicp.jetcache.support.CacheResult;
import com.alicp.jetcache.support.CacheResultCode;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public abstract class AbstractLocalCacheTest {
    protected Cache cache;
    protected CacheConfig cc;

    protected abstract void setup(boolean useSofeRef);

    public void test1() throws Exception {
        for (int i = 0; i < 2; i++) {
            setup(i == 0);
            Assert.assertEquals(CacheResultCode.NOT_EXISTS, cache.get(cc, "S1", "K1").getResultCode());
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.put(cc, "S1", "K1", "V1", System.currentTimeMillis() +1000));
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.get(cc, "S1", "K1").getResultCode());
            Assert.assertEquals("V1", cache.get(cc, "S1", "K1").getValue());
            Assert.assertEquals(CacheResultCode.NOT_EXISTS, cache.get(cc, "S2", "K1").getResultCode());
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.put(cc, "S1", "K1", "V2", System.currentTimeMillis() +1000));
            Assert.assertEquals("V2", cache.get(cc, "S1", "K1").getValue());

            cc.setArea("A2");
            Assert.assertEquals(CacheResultCode.NOT_EXISTS, cache.get(cc, "S1", "K1").getResultCode());
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.put(cc, "S1", "K1", "V1", System.currentTimeMillis() +1000));
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.get(cc, "S1", "K1").getResultCode());
            Assert.assertEquals("V1", cache.get(cc, "S1", "K1").getValue());
        }
    }

    public void testLRU1() {
        for (int i = 0; i < 2; i++) {
            setup(i == 0);
            cc.setLocalLimit(2);
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.put(cc, "S1", "K1", "V1", System.currentTimeMillis() +1000));
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.put(cc, "S1", "K2", "V2", System.currentTimeMillis() +1000));
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.put(cc, "S1", "K3", "V3", System.currentTimeMillis() +1000));
            Assert.assertEquals(CacheResultCode.NOT_EXISTS, cache.get(cc, "S1", "K1").getResultCode());
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.get(cc, "S1", "K2").getResultCode());
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.get(cc, "S1", "K3").getResultCode());
        }
    }

    public void testLRU2() {
        for (int i = 0; i < 2; i++) {
            setup(i == 0);
            cc.setLocalLimit(2);
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.put(cc, "S1", "K1", "V1", System.currentTimeMillis() +1000));
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.put(cc, "S1", "K2", "V2", System.currentTimeMillis() +1000));
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.get(cc, "S1", "K1").getResultCode());
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.put(cc, "S1", "K3", "V3", System.currentTimeMillis() +1000));
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.get(cc, "S1", "K1").getResultCode());
            Assert.assertEquals(CacheResultCode.NOT_EXISTS, cache.get(cc, "S1", "K2").getResultCode());
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.get(cc, "S1", "K3").getResultCode());
        }
    }

    public void testExpire() throws Exception {
        for (int i = 0; i < 2; i++) {
            setup(i == 0);
            long expireTime = System.currentTimeMillis() +100;
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.put(cc, "S1", "K1", "V1", expireTime));

            CacheResult result = cache.get(cc, "S1", "K1");
            Assert.assertEquals(CacheResultCode.SUCCESS, result.getResultCode());
            Assert.assertEquals("V1", result.getValue());
            Assert.assertEquals(expireTime, result.getExpireTime());

            Thread.sleep(101);
            result = cache.get(cc, "S1", "K1");
            Assert.assertEquals(CacheResultCode.EXPIRED, result.getResultCode());
            Assert.assertNull(result.getValue());
            Assert.assertEquals(0, result.getExpireTime());
        }
    }

    public void testNull() {
        for (int i = 0; i < 2; i++) {
            setup(i == 0);
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.put(cc, "S1", "K1", null, System.currentTimeMillis() + 1000));
            Assert.assertEquals(CacheResultCode.SUCCESS, cache.get(cc, "S1", "K1").getResultCode());
            Assert.assertNull(cache.get(cc, "S1", "K1").getValue());
        }
    }

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
                        cache.put(cc, subArea, key, value, System.currentTimeMillis() + CacheConsts.DEFAULT_EXPIRE * 1000);
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

        Thread.sleep(1000);
        for (int i = 0; i < threadCount; i++) {
            t[i].stop = true;
        }

        if (cocurrentFail) {
            Assert.fail();
        }
    }
}
