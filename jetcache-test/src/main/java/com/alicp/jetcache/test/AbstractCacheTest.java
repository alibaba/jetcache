package com.alicp.jetcache.test;

import com.alicp.jetcache.*;
import com.alicp.jetcache.test.support.DynamicQuery;
import org.junit.Assert;

import java.io.Serializable;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created on 2016/10/8.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public abstract class AbstractCacheTest {
    protected Cache<Object, Object> cache;

    protected void baseTest() throws Exception {
        // get/put/get
        Assert.assertEquals(CacheResultCode.NOT_EXISTS, cache.GET("BASE_K1").getResultCode());
        Assert.assertEquals(CacheResultCode.SUCCESS, cache.PUT("BASE_K1", "V1", 10, TimeUnit.SECONDS).getResultCode());
        Assert.assertEquals(CacheResultCode.SUCCESS, cache.GET("BASE_K1").getResultCode());
        Assert.assertEquals("V1", cache.GET("BASE_K1").getValue());

        // update
        Assert.assertEquals(CacheResultCode.SUCCESS, cache.PUT("BASE_K1", "V2", 10, TimeUnit.SECONDS).getResultCode());
        Assert.assertEquals("V2", cache.GET("BASE_K1").getValue());

        //remove
        Assert.assertEquals(CacheResultCode.SUCCESS, cache.REMOVE("BASE_K1").getResultCode());
        Assert.assertEquals(CacheResultCode.NOT_EXISTS, cache.GET("BASE_K1").getResultCode());

        // null value
        cache.put("BASE_K2", null);
        CacheGetResult<Object> r = cache.GET("BASE_K2");
        Assert.assertTrue(r.isSuccess());
        Assert.assertNull(r.getValue());

        computeIfAbsentTest();
        lockTest();
        putIfAbsentTest();
        complextValueTest();
    }

    private boolean isMultiLevelCache() {
        Cache c = cache;
        while (c instanceof ProxyCache) {
            c = ((ProxyCache) c).getTargetCache();
        }
        return c instanceof MultiLevelCache;
    }

    private void putIfAbsentTest() {
        if (isMultiLevelCache()) {
            return;
        }
        Assert.assertTrue(cache.putIfAbsent("PIA_K1", "V1"));
        Assert.assertFalse(cache.putIfAbsent("PIA_K1", "V1"));
        Assert.assertEquals("V1", cache.get("PIA_K1"));
        Assert.assertTrue(cache.remove("PIA_K1"));

        Assert.assertEquals(CacheResultCode.SUCCESS, cache.PUT_IF_ABSENT("PIA_K2", "V2", 10, TimeUnit.SECONDS).getResultCode());
        Assert.assertEquals(CacheResultCode.EXISTS, cache.PUT_IF_ABSENT("PIA_K2", "V2", 10, TimeUnit.SECONDS).getResultCode());
        Assert.assertEquals("V2", cache.get("PIA_K2"));
        Assert.assertTrue(cache.remove("PIA_K2"));
    }

    private void computeIfAbsentTest() {
        //computeIfAbsent
        {
            cache.put("CIA_K1", "V");
            cache.computeIfAbsent("CIA_K1", k -> {
                throw new RuntimeException();
            });
            Assert.assertEquals("AAA", cache.computeIfAbsent("CIA_NOT_EXIST_1", k -> "AAA"));
            Assert.assertNull(cache.computeIfAbsent("CIA_NOT_EXIST_2", k -> null));
            final Object[] invoked = new Object[1];
            Assert.assertNull(cache.computeIfAbsent("CIA_NOT_EXIST_2", k -> {
                invoked[0] = new Object();
                return null;
            }));
            Assert.assertNotNull(invoked[0]);
            Assert.assertNull(cache.computeIfAbsent("CIA_NOT_EXIST_3", k -> null, true));
            Assert.assertNull(cache.computeIfAbsent("CIA_NOT_EXIST_3", k -> {
                throw new RuntimeException();
            }, true));
        }

        {
            cache.put("CIA_K2", "V");
            cache.computeIfAbsent("CIA_K2", k -> {
                throw new RuntimeException();
            }, false, 1, TimeUnit.MINUTES);
            Assert.assertEquals("AAA", cache.computeIfAbsent("CIA_NOT_EXIST_11", k -> "AAA", false, 1, TimeUnit.MINUTES));
            Assert.assertNull(cache.computeIfAbsent("CIA_NOT_EXIST_22", k -> null, false, 1, TimeUnit.MINUTES));
            final Object[] invoked = new Object[1];
            Assert.assertNull(cache.computeIfAbsent("CIA_NOT_EXIST_22", k -> {
                invoked[0] = new Object();
                return null;
            }));
            Assert.assertNotNull(invoked[0]);
            Assert.assertNull(cache.computeIfAbsent("CIA_NOT_EXIST_33", k -> null, true, 1, TimeUnit.MINUTES));
            Assert.assertNull(cache.computeIfAbsent("CIA_NOT_EXIST_33", k -> {
                throw new RuntimeException();
            }, true, 1, TimeUnit.MINUTES));
        }
    }


    static class A implements Serializable {
        private static final long serialVersionUID = 1692575072446353143L;

        public A(){
        }
        int id;
        String name;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public int hashCode() {
            return id;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            return ((A) obj).id == id;
        }
    }
    private void complextValueTest() {
        A a1 = new A();
        A a2 = new A();
        A a3 = new A();
        a1.id = 100;
        a2.id = 100;
        a3.id = 101;
        a1.name = "N1";
        a1.name = "N2";
        a1.name = "N3";

        cache.put("CVT_K1", a1);
        A fromCache = (A) cache.get("CVT_K1");
        Assert.assertEquals(a2, fromCache);
        Assert.assertNotEquals(a3, fromCache);

    }

    protected void lockTest() throws Exception {
        try (AutoReleaseLock lock = cache.tryLock("LockKey1", 200, TimeUnit.HOURS)) {
            Assert.assertNotNull(lock);
            Assert.assertNull(cache.tryLock("LockKey1", 200, TimeUnit.HOURS));
            Assert.assertNotNull(cache.tryLock("LockKey2", 200, TimeUnit.MILLISECONDS));
        }
        Assert.assertNotNull(cache.tryLock("LockKey1", 50, TimeUnit.MILLISECONDS));
        Assert.assertNull(cache.tryLock("LockKey1", 50, TimeUnit.MILLISECONDS));
        Thread.sleep(50);
        Assert.assertNotNull(cache.tryLock("LockKey1", 50, TimeUnit.MILLISECONDS));
    }

    protected void expireAfterWriteTest(long ttl) throws InterruptedException {
        cache.put("EXPIRE_W_K1", "V1");
        expireAfterWriteTestImpl(ttl);

        ttl = ttl / 2;
        cache.put("EXPIRE_W_K1", "V1", ttl, TimeUnit.MILLISECONDS);
        expireAfterWriteTestImpl(ttl);
    }

    protected void expireAfterAccessTest(long ttl) throws InterruptedException {
        Assert.assertEquals(CacheResultCode.SUCCESS, cache.PUT("EXPIRE_A_K1", "V1").getResultCode());
        expireAfterAccessTestImpl(ttl);

        ttl = ttl / 2;
        Assert.assertEquals(CacheResultCode.SUCCESS, cache.PUT("EXPIRE_A_K1", "V1", ttl, TimeUnit.MILLISECONDS).getResultCode());
        expireAfterAccessTestImpl(ttl);
    }

    protected void expireAfterWriteTestImpl(long ttl) throws InterruptedException {
        CacheGetResult r = cache.GET("EXPIRE_W_K1");
        Assert.assertEquals(CacheResultCode.SUCCESS, r.getResultCode());
        Assert.assertEquals("V1", r.getValue());

        Thread.sleep(ttl / 2);
        r = cache.GET("EXPIRE_W_K1");
        Assert.assertEquals(CacheResultCode.SUCCESS, r.getResultCode());
        Assert.assertEquals("V1", r.getValue());

        Thread.sleep(ttl / 2 + 2);
        r = cache.GET("EXPIRE_W_K1");
        Assert.assertTrue(r.getResultCode() == CacheResultCode.EXPIRED || r.getResultCode() == CacheResultCode.NOT_EXISTS);
        Assert.assertNull(r.getValue());
    }

    protected void expireAfterAccessTestImpl(long ttl) throws InterruptedException {
        Assert.assertEquals("V1", cache.get("EXPIRE_A_K1"));
        Thread.sleep(ttl / 2);
        Assert.assertEquals("V1", cache.get("EXPIRE_A_K1"));
        Thread.sleep(ttl / 2 + 2);
        Assert.assertEquals("V1", cache.get("EXPIRE_A_K1"));
        Thread.sleep(ttl + 1);
        CacheGetResult<Object> r = cache.GET("EXPIRE_A_K1");
        Assert.assertTrue(r.getResultCode() == CacheResultCode.EXPIRED || r.getResultCode() == CacheResultCode.NOT_EXISTS);
        Assert.assertNull(r.getValue());
    }

    protected void keyCoverterTest() {
        DynamicQuery d1 = new DynamicQuery();
        DynamicQuery d2 = new DynamicQuery();
        DynamicQuery d3 = new DynamicQuery();
        d1.setId(100);
        d2.setId(100);
        d3.setId(101);
        d1.setName("HL");
        d2.setName("HL");

        cache.put(d1, "V1");
        Assert.assertEquals("V1", cache.get(d2));
        Assert.assertNull(cache.get(d3));
    }


    private volatile boolean cocurrentFail = false;


    private volatile AtomicLong lockCount1;
    private volatile AtomicLong lockCount2;
    private volatile AtomicLong lockAtommicCount1;
    private volatile AtomicLong lockAtommicCount2;

    protected void concurrentTest(int threadCount, int timeInMillis) throws Exception {
        int count = 100;
        lockAtommicCount1 = new AtomicLong();
        lockAtommicCount2 = new AtomicLong();
        lockCount1 = new AtomicLong();
        lockCount2 = new AtomicLong();
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        class T extends Thread {
            private String keyPrefix;
            private transient boolean stop;
            private Random random = new Random();

            private T(String keyPrefix) {
                this.keyPrefix = keyPrefix;
            }

            @Override
            public void run() {
                try {
                    int i = 0;
                    while (!stop) {
                        if (++i >= count) {
                            i = 0;
                        }
                        String key = keyPrefix + i;
                        Integer value = random.nextInt(10);

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
                        Assert.assertTrue(cache.remove(key));

                        if (!isMultiLevelCache()) {
                            cache.putIfAbsent(String.valueOf(i), i);
                        }

                        boolean b = random.nextBoolean();
                        String lockKey = b ? "locka" : "lockb";
                        try (AutoReleaseLock lock = cache.tryLock(lockKey, 10, TimeUnit.SECONDS)) {
                            if (lock != null) {
                                int x = random.nextInt(10);
                                AtomicLong lockAtomicCount = b ? lockAtommicCount1 : lockAtommicCount2;
                                AtomicLong lockCount = b ? lockCount1 : lockCount2;
                                String shareKey = lockKey + "_share";

                                lockAtomicCount.addAndGet(x);

                                cache.put(shareKey, x);
                                Assert.assertEquals(x, cache.get(shareKey));
                                Assert.assertTrue(cache.remove(shareKey));
                                if (b) {
                                    putIfAbsentTest();
                                }

                                lockCount.set(lockCount.get() + x);

                            }
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    cocurrentFail = true;
                }
                countDownLatch.countDown();
            }
        }

        T[] t = new T[threadCount];
        for (int i = 0; i < threadCount; i++) {
            t[i] = new T("T" + i + "_");
            t[i].setName("ConTest" + i);
            t[i].start();
        }

        Thread.sleep(timeInMillis);
        for (int i = 0; i < threadCount; i++) {
            t[i].stop = true;
        }
        countDownLatch.await();

        Assert.assertEquals(lockAtommicCount1.get(), lockCount1.get());
        Assert.assertEquals(lockAtommicCount2.get(), lockCount2.get());

        Assert.assertFalse(cocurrentFail);
    }
}
