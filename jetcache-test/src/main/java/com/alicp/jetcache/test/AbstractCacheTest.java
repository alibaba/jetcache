package com.alicp.jetcache.test;

import com.alicp.jetcache.*;
import com.alicp.jetcache.test.support.DynamicQuery;
import org.junit.Assert;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * Created on 2016/10/8.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public abstract class AbstractCacheTest {
    protected Cache<Object, Object> cache;

    protected void baseTest() throws Exception {
        illegalArgTest();

        // get/put/getByMethodInfo
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

        getAllTest();
        putAllTest();
        removeAllTest();

        computeIfAbsentTest();
        lockTest();
        putIfAbsentTest();
        complextValueTest();

        asyncTest();

        penetrationProtectTest(cache);
    }

    private void illegalArgTest() {
        Assert.assertNull(cache.get(null));
        Assert.assertEquals(CacheResultCode.FAIL, cache.GET(null).getResultCode());
        Assert.assertEquals(CacheResult.MSG_ILLEGAL_ARGUMENT, cache.GET(null).getMessage());

        Assert.assertNull(cache.getAll(null));
        Assert.assertEquals(CacheResultCode.FAIL, cache.GET_ALL(null).getResultCode());
        Assert.assertEquals(CacheResult.MSG_ILLEGAL_ARGUMENT, cache.GET_ALL(null).getMessage());

        Assert.assertEquals(CacheResultCode.FAIL, cache.PUT(null, "V1").getResultCode());
        Assert.assertEquals(CacheResult.MSG_ILLEGAL_ARGUMENT, cache.PUT(null, "V1").getMessage());

        Assert.assertEquals(CacheResultCode.FAIL, cache.PUT(null, "V1", 1, TimeUnit.SECONDS).getResultCode());
        Assert.assertEquals(CacheResult.MSG_ILLEGAL_ARGUMENT, cache.PUT(null, "V1", 1, TimeUnit.SECONDS).getMessage());

        Assert.assertEquals(CacheResultCode.FAIL, cache.PUT_ALL(null).getResultCode());
        Assert.assertEquals(CacheResult.MSG_ILLEGAL_ARGUMENT, cache.PUT_ALL(null).getMessage());

        Assert.assertEquals(CacheResultCode.FAIL, cache.PUT_ALL(null, 1, TimeUnit.SECONDS).getResultCode());
        Assert.assertEquals(CacheResult.MSG_ILLEGAL_ARGUMENT, cache.PUT_ALL(null, 1, TimeUnit.SECONDS).getMessage());

        try {
            Assert.assertFalse(cache.putIfAbsent(null, "V1"));
            Assert.assertEquals(CacheResultCode.FAIL, cache.PUT_IF_ABSENT(null, "V1", 1, TimeUnit.SECONDS).getResultCode());
            Assert.assertEquals(CacheResult.MSG_ILLEGAL_ARGUMENT, cache.PUT_IF_ABSENT(null, "V1", 1, TimeUnit.SECONDS).getMessage());
        } catch (UnsupportedOperationException e) {
            Cache c = cache;
            while (c instanceof ProxyCache) {
                c = ((ProxyCache) c).getTargetCache();
            }
            if (c instanceof MultiLevelCache) {
                // OK
            } else {
                Assert.fail();
            }
        }

        Assert.assertFalse(cache.remove(null));
        Assert.assertEquals(CacheResultCode.FAIL, cache.REMOVE(null).getResultCode());
        Assert.assertEquals(CacheResult.MSG_ILLEGAL_ARGUMENT, cache.REMOVE(null).getMessage());

        Assert.assertEquals(CacheResultCode.FAIL, cache.REMOVE_ALL(null).getResultCode());
        Assert.assertEquals(CacheResult.MSG_ILLEGAL_ARGUMENT, cache.REMOVE_ALL(null).getMessage());

        try {
            cache.unwrap(String.class);
            Assert.fail();
        } catch (IllegalArgumentException e) {
        }

        Assert.assertNull(cache.tryLock(null, 1, TimeUnit.SECONDS));
        cache.tryLockAndRun(null, 1, TimeUnit.SECONDS, () -> Assert.fail());
    }

    private void getAllTest() {
        String k1 = "getAllTest_K1", k2 = "getAllTest_K2", k3 = "getAllTest_K3";
        HashSet s = new HashSet();
        s.add(k1);
        s.add(k2);
        s.add(k3);
        cache.put(k1, "V1");
        cache.put(k2, "V2");

        MultiGetResult<Object, Object> r = cache.GET_ALL(s);
        Assert.assertTrue(r.isSuccess());
        Assert.assertEquals(3, r.getValues().size());
        Assert.assertTrue(r.getValues().get(k1).isSuccess());
        Assert.assertEquals("V1", r.getValues().get(k1).getValue());
        Assert.assertTrue(r.getValues().get(k2).isSuccess());
        Assert.assertEquals("V2", r.getValues().get(k2).getValue());
        Assert.assertEquals(CacheResultCode.NOT_EXISTS, r.getValues().get(k3).getResultCode());
        Assert.assertNull(r.getValues().get(k3).getValue());

        Map<Object, Object> map = cache.getAll(s);
        Assert.assertEquals(2, map.size());
        Assert.assertEquals("V1", map.get(k1));
        Assert.assertEquals("V2", map.get(k2));
        Assert.assertNull(map.get(k3));

        Assert.assertEquals(0, cache.getAll(Collections.emptySet()).size());
    }

    private void putAllTest() throws Exception {
        String k1 = "putAllTest_K1", k2 = "putAllTest_K2", k3 = "putAllTest_K3";
        String k4 = "putAllTest_K4", k5 = "putAllTest_K5", k6 = "putAllTest_K6";
        String k7 = "putAllTest_K7", k8 = "putAllTest_K8", k9 = "putAllTest_K9";
        Map m = new HashMap();
        m.put(k1, "V1");
        m.put(k2, "V2");
        m.put(k3, "V3");
        Assert.assertTrue(cache.PUT_ALL(m).isSuccess());
        Assert.assertEquals("V1", cache.get(k1));
        Assert.assertEquals("V2", cache.get(k2));
        Assert.assertEquals("V3", cache.get(k3));

        m.clear();
        m.put(k4, "V4");
        m.put(k5, "V5");
        m.put(k6, "V6");
        cache.putAll(m);
        Assert.assertEquals("V4", cache.get(k4));
        Assert.assertEquals("V5", cache.get(k5));
        Assert.assertEquals("V6", cache.get(k6));

        m.clear();
        m.put(k7, "V7");
        m.put(k8, "V8");
        m.put(k9, "V9");
        Assert.assertTrue(cache.PUT_ALL(m, 5000, TimeUnit.MILLISECONDS).isSuccess());
        Assert.assertEquals("V7", cache.get(k7));
        Assert.assertEquals("V8", cache.get(k8));
        Assert.assertEquals("V9", cache.get(k9));

        m.clear();
        m.put(k7, "V77");
        m.put(k8, "V88");
        m.put(k9, "V99");
        cache.putAll(m, 5000, TimeUnit.MILLISECONDS);
        Assert.assertEquals("V77", cache.get(k7));
        Assert.assertEquals("V88", cache.get(k8));
        Assert.assertEquals("V99", cache.get(k9));
    }

    private void removeAllTest() {
        String k1 = "removeAllTest_K1", k2 = "removeAllTest_K2", k3 = "removeAllTest_K3";
        cache.put(k1, "V1");
        cache.put(k2, "V2");
        cache.put(k3, "V3");

        HashSet s = new HashSet();
        s.add(k1);
        s.add(k2);
        cache.removeAll(s);
        Assert.assertNull(cache.get(k1));
        Assert.assertNull(cache.get(k2));
        Assert.assertNotNull(cache.get(k3));

        s = new HashSet();
        s.add(k1);
        s.add(k3);
        Assert.assertTrue(cache.REMOVE_ALL(s).isSuccess());
        Assert.assertNull(cache.get(k1));
        Assert.assertNull(cache.get(k2));
        Assert.assertNull(cache.get(k3));
    }

    private boolean isMultiLevelCache() {
        Cache c = cache;
        while (c instanceof ProxyCache) {
            c = ((ProxyCache) c).getTargetCache();
        }
        return c instanceof MultiLevelCache;
    }

    private void putIfAbsentTest() throws Exception {
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

        Assert.assertTrue(cache.PUT_IF_ABSENT("PIA_K3", "V3", 5, TimeUnit.MILLISECONDS).isSuccess());
        Thread.sleep(10);
        Assert.assertTrue(cache.PUT_IF_ABSENT("PIA_K3", "V3", 5, TimeUnit.MILLISECONDS).isSuccess());
        cache.remove("PIA_K3");
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

    boolean asyncTestFail = false;

    private void asyncTest() throws Exception {
        CacheResult putResult = cache.PUT("async_K1", "V1");
        putResult.future().thenAccept(resultData -> {
            if (resultData.getResultCode() != CacheResultCode.SUCCESS) {
                asyncTestFail = true;
            }
        });
        putResult.future().toCompletableFuture().get();
        Assert.assertFalse(asyncTestFail);

        CacheGetResult getResult = cache.GET("async_K1");
        getResult.future().thenAccept(resultData -> {
            if (resultData.getResultCode() != CacheResultCode.SUCCESS) {
                asyncTestFail = true;
            }
            if (!"V1".equals(resultData.getData())) {
                asyncTestFail = true;
            }
        });
        getResult.future().toCompletableFuture().get();
        Assert.assertFalse(asyncTestFail);

        CacheGetResult getResult2 = cache.GET("async_K1");
        getResult2.future().thenRun(() -> {
            if (!"V1".equals(getResult2.getValue())) {
                asyncTestFail = true;
            }
        });
        getResult2.future().toCompletableFuture().get();
        Assert.assertFalse(asyncTestFail);

        HashSet<String> s = new HashSet<>();
        s.add("async_K1");
        s.add("async_K2");
        MultiGetResult multiGetResult = cache.GET_ALL(s);
        multiGetResult.future().thenAccept(resultData -> {
            if (resultData.getResultCode() != CacheResultCode.SUCCESS) {
                asyncTestFail = true;
            }
            Map m = (Map) resultData.getData();
            CacheGetResult r1 = (CacheGetResult) m.get("async_K1");
            CacheGetResult r2 = (CacheGetResult) m.get("async_K2");
            if (r1.getResultCode() != CacheResultCode.SUCCESS) {
                asyncTestFail = true;
            }
            if (r2.getResultCode() != CacheResultCode.NOT_EXISTS) {
                asyncTestFail = true;
            }
            if (!"V1".equals(r1.getValue())) {
                asyncTestFail = true;
            }
            if (r2.getValue() != null) {
                asyncTestFail = true;
            }
        });
        multiGetResult.future().toCompletableFuture().get();
        Assert.assertFalse(asyncTestFail);

        MultiGetResult multiGetResult2 = cache.GET_ALL(s);
        multiGetResult2.future().thenRun(() -> {
            if (multiGetResult2.getResultCode() != CacheResultCode.SUCCESS) {
                asyncTestFail = true;
            }
            Map m = multiGetResult2.unwrapValues();
            if (!"V1".equals(m.get("async_K1"))) {
                asyncTestFail = true;
            }
            if (m.get("async_K2") != null) {
                asyncTestFail = true;
            }
        });
        multiGetResult2.future().toCompletableFuture().get();
        Assert.assertFalse(asyncTestFail);

        CacheResult removeResult = cache.REMOVE("async_K1");
        removeResult.future().thenAccept(resultData -> {
            if (resultData.getResultCode() != CacheResultCode.SUCCESS) {
                asyncTestFail = true;
            }
        });
        removeResult.future().toCompletableFuture().get();
        Assert.assertFalse(asyncTestFail);
    }


    static class A implements Serializable {
        private static final long serialVersionUID = 1692575072446353143L;

        public A() {
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
        Thread.sleep(51);
        Assert.assertNotNull(cache.tryLock("LockKey1", 50, TimeUnit.MILLISECONDS));

        int count = 10;
        CountDownLatch countDownLatch = new CountDownLatch(count);
        int[] runCount = new int[2];
        Runnable runnable = () -> {
            boolean b = cache.tryLockAndRun("LockKeyAndRunKey", 10, TimeUnit.SECONDS,
                    () -> {
                        runCount[1]++;
                        while (countDownLatch.getCount() > 1) {
                            try {
                                Thread.sleep(1);
                            } catch (InterruptedException e) {
                            }
                        }
                    });
            if (b)
                runCount[0]++;
            countDownLatch.countDown();
        };
        for (int i = 0; i < count; i++) {
            new Thread(runnable).start();
        }
        countDownLatch.await();
        Assert.assertEquals(1, runCount[0]);
        Assert.assertEquals(1, runCount[1]);

        try {
            cache.tryLockAndRun("LockKeyAndRunKey", 10, TimeUnit.SECONDS, () -> {
                throw new RuntimeException();
            });
            Assert.fail();
        } catch (Exception e) {
            try (AutoReleaseLock lock = cache.tryLock("LockKeyAndRunKey", 1, TimeUnit.SECONDS)) {
                Assert.assertNotNull(lock);
            }
            ;
        }
    }

    protected void expireAfterWriteTest(long ttl) throws InterruptedException {
        cache.put("EXPIRE_W_K1", "V1");
        expireAfterWriteTestImpl("EXPIRE_W_K1", ttl);

        HashMap m = new HashMap();
        m.put("EXPIRE_W_K2", "V1");
        cache.PUT_ALL(m);
        expireAfterWriteTestImpl("EXPIRE_W_K2", ttl);

        ttl = ttl / 2;

        cache.put("EXPIRE_W_K3", "V1", ttl, TimeUnit.MILLISECONDS);
        expireAfterWriteTestImpl("EXPIRE_W_K3", ttl);

        m = new HashMap();
        m.put("EXPIRE_W_K4", "V1");
        cache.PUT_ALL(m, ttl, TimeUnit.MILLISECONDS);
        expireAfterWriteTestImpl("EXPIRE_W_K4", ttl);
    }

    protected void expireAfterAccessTest(long ttl) throws InterruptedException {
        Assert.assertEquals(CacheResultCode.SUCCESS, cache.PUT("EXPIRE_A_K1", "V1").getResultCode());
        expireAfterAccessTestImpl("EXPIRE_A_K1", ttl);

        HashMap m = new HashMap();
        m.put("EXPIRE_W_K2", "V1");
        cache.PUT_ALL(m);
        expireAfterAccessTestImpl("EXPIRE_W_K2", ttl);

        m = new HashMap();
        m.put("EXPIRE_W_K4", "V1");
        cache.PUT_ALL(m);
        expireAfterAccessTestImpl("EXPIRE_W_K4", ttl);
    }

    protected void expireAfterWriteTestImpl(String key, long ttl) throws InterruptedException {
        CacheGetResult r = cache.GET(key);
        Assert.assertEquals(CacheResultCode.SUCCESS, r.getResultCode());
        Assert.assertEquals("V1", r.getValue());

        Thread.sleep(ttl / 2);
        r = cache.GET(key);
        Assert.assertEquals(CacheResultCode.SUCCESS, r.getResultCode());
        Assert.assertEquals("V1", r.getValue());

        Thread.sleep(ttl / 2 + 2);
        r = cache.GET(key);
        Assert.assertTrue(r.getResultCode() == CacheResultCode.EXPIRED || r.getResultCode() == CacheResultCode.NOT_EXISTS);
        Assert.assertNull(r.getValue());
    }

    protected void expireAfterAccessTestImpl(String key, long ttl) throws InterruptedException {
        Assert.assertEquals("V1", cache.get(key));
        Thread.sleep(ttl / 2);
        Assert.assertEquals("V1", cache.get(key));
        Thread.sleep(ttl / 2 + 2);
        Assert.assertEquals("V1", cache.get(key));
        Thread.sleep(ttl + 1);
        CacheGetResult<Object> r = cache.GET(key);
        Assert.assertTrue(r.getResultCode() == CacheResultCode.EXPIRED || r.getResultCode() == CacheResultCode.NOT_EXISTS);
        Assert.assertNull(r.getValue());
    }

    protected void fastjsonKeyCoverterTest() {
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

    protected void concurrentTest(int threadCount, int limit, int timeInMillis) throws Exception {
        concurrentTest(threadCount, limit * 5, timeInMillis, true);
        concurrentTest(threadCount, limit, timeInMillis, false);
    }

    private void concurrentTest(int threadCount, int limit, int timeInMillis, boolean overflow) throws Exception {
        int count = limit / threadCount;
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

            private void task1() {
                int i = 0;
                while (!stop) {
                    if (++i >= count) {
                        i = 0;
                    }
                    String key = keyPrefix + i;
                    Integer value = random.nextInt(10);

                    cache.PUT(key, value, 10000, TimeUnit.SECONDS);
                    CacheGetResult result = cache.GET(key);
                    checkResult(key, value, result);
                    CacheResult removeResult = cache.REMOVE(key);
                    Assert.assertTrue(removeResult.isSuccess() || removeResult.getResultCode() == CacheResultCode.NOT_EXISTS);

                    if (!isMultiLevelCache()) {
                        cache.putIfAbsent(String.valueOf(i), i);
                    }

                    String k1 = String.valueOf(i);
                    String k2 = key;
                    HashMap m = new HashMap();
                    m.put(k1, value);
                    value = value + 1;
                    m.put(k2, value);
                    Assert.assertTrue(cache.PUT_ALL(m).isSuccess());
                    MultiGetResult<Object, Object> multiGetResult = cache.GET_ALL(m.keySet());
                    Assert.assertTrue(multiGetResult.isSuccess());
                    checkResult(k2, value, multiGetResult.getValues().get(k2));
                    Assert.assertTrue(cache.REMOVE_ALL(m.keySet()).isSuccess());
                }
            }

            private void task2() throws Exception {
                while (!stop) {
                    boolean b = random.nextBoolean();
                    String lockKey = b ? "lock1" : "lock2";
                    try (AutoReleaseLock lock = cache.tryLock(lockKey, 10, TimeUnit.SECONDS)) {
                        if (lock != null) {
                            int x = random.nextInt(10);
                            AtomicLong lockAtomicCount = b ? lockAtommicCount1 : lockAtommicCount2;
                            AtomicLong lockCount = b ? lockCount1 : lockCount2;
                            String shareKey = lockKey + "_share";

                            lockAtomicCount.addAndGet(x);
                            long lockCountNum = lockCount.get();

                            cache.put(shareKey, x);
                            Assert.assertEquals(x, cache.get(shareKey));
                            Assert.assertTrue(cache.remove(shareKey));
                            if (b) {
                                putIfAbsentTest();
                            }

                            lockCount.set(lockCountNum + x);
                        }
                    }
                }
            }

            @Override
            public void run() {
                try {
                    if (overflow) {
                        task1();
                    } else {
                        task2();
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    cocurrentFail = true;
                }
                countDownLatch.countDown();
            }

            private void checkResult(String key, Integer value, CacheGetResult result) {
                if (result.getResultCode() != CacheResultCode.SUCCESS && result.getResultCode() != CacheResultCode.NOT_EXISTS) {
                    System.out.println("key:" + key + ",code:" + result.getResultCode());
                    cocurrentFail = true;
                }
                if (result.isSuccess() && !result.getValue().equals(value)) {
                    System.out.println("key:" + key + ",value:" + result.getValue());
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

        Thread.sleep(timeInMillis);
        for (int i = 0; i < threadCount; i++) {
            t[i].stop = true;
        }
        countDownLatch.await();

        Assert.assertEquals(lockAtommicCount1.get(), lockCount1.get());
        Assert.assertEquals(lockAtommicCount2.get(), lockCount2.get());

        Assert.assertFalse(cocurrentFail);
    }

    public static void penetrationProtectTest(Cache cache) throws Exception {
        boolean oldPenetrationProtect = cache.config().isCachePenetrationProtect();
        cache.config().setCachePenetrationProtect(true);

        penetrationProtectTestWithComputeIfAbsent(cache);
        if (cache instanceof LoadingCache) {
            penetrationProtectTestWithLoadingCache(cache);
        }

        cache.config().setCachePenetrationProtect(oldPenetrationProtect);
    }

    private static void penetrationProtectTestWithComputeIfAbsent(Cache cache) throws Exception {
        String keyPrefix = "penetrationProtect_";

        AtomicInteger loadSuccess = new AtomicInteger(0);
        Function loader = new Function() {
            private AtomicInteger count1 = new AtomicInteger(0);
            private AtomicInteger count2 = new AtomicInteger(0);

            @Override
            public Object apply(Object k) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if ((keyPrefix + "1").equals(k)) {
                    // fail 2 times
                    if (count1.getAndIncrement() <= 1)
                        throw new RuntimeException("mock error");
                } else if ((keyPrefix + "2").equals(k)) {
                    // fail 3 times
                    if (count2.getAndIncrement() <= 2)
                        throw new RuntimeException("mock error");
                }
                loadSuccess.incrementAndGet();
                return k + "_V";
            }
        };

        int threadCount = 20;
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        AtomicInteger getFailCount = new AtomicInteger(0);
        AtomicBoolean fail = new AtomicBoolean(false);
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            Thread t = new Thread(() -> {
                String key = keyPrefix + (index % 3);
                try {
                    Object o = cache.computeIfAbsent(key, loader);
                    if (!o.equals(key + "_V")) {
                        fail.set(true);
                    }
                } catch (Throwable e) {
                    if(!"mock error".equals(e.getMessage())){
                        e.printStackTrace();
                    }
                    getFailCount.incrementAndGet();
                }
                countDownLatch.countDown();
            });
            t.start();
        }
        countDownLatch.await();

        Assert.assertFalse(fail.get());
        Assert.assertEquals(3, loadSuccess.get());
        Assert.assertEquals(2 + 3, getFailCount.get());

        cache.remove(keyPrefix + "0");
        cache.remove(keyPrefix + "1");
        cache.remove(keyPrefix + "2");
    }

    private static void penetrationProtectTestWithLoadingCache(Cache cache) throws Exception {
        String failMsg[] = new String[1];

        Function<Integer, Integer> loaderFunction = new Function<Integer, Integer>() {
            ConcurrentHashMap<Integer, Integer> map = new ConcurrentHashMap<>();
            @Override
            public Integer apply(Integer key) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (map.get(key) == null) {
                    map.put(key, key);
                } else {
                    failMsg[0] = "each key should load only once";
                }
                return key + 100;
            }
        };
        CacheLoader<Integer, Integer> loader = (k) -> loaderFunction.apply(k);

        CacheLoader oldLoader = cache.config().getLoader();
        cache.config().setLoader(loader);

        CountDownLatch countDownLatch = new CountDownLatch(5);
        Cache<Integer, Integer> c = cache;
        new Thread(() -> {
            if (c.get(2000) != 2100) {
                failMsg[0] = "value error";
            }
            countDownLatch.countDown();
        }).start();
        new Thread(() -> {
            if (c.get(2000) != 2100) {
                failMsg[0] = "value error";
            }
            countDownLatch.countDown();
        }).start();
        new Thread(() -> {
            if (c.get(2001) != 2101) {
                failMsg[0] = "value error";
            }
            countDownLatch.countDown();
        }).start();
        new Thread(() -> {
            if (c.computeIfAbsent(2001, loaderFunction) != 2101) {
                failMsg[0] = "value error";
            }
            countDownLatch.countDown();
        }).start();
        new Thread(() -> {
            Set<Integer> s = new HashSet<>();
            s.add(2001);
            s.add(2002);
            Map<Integer, Integer> values = c.getAll(s);
            if (values.get(2001) != 2101) {
                failMsg[0] = "value error";
            }
            if (values.get(2002) != 2102) {
                failMsg[0] = "value error";
            }
            countDownLatch.countDown();
        }).start();
        countDownLatch.await();

        Assert.assertNull(failMsg[0]);

        cache.config().setLoader(oldLoader);
    }
}
