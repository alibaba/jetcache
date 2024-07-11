package com.alicp.jetcache.anno.method;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.anno.*;
import com.alicp.jetcache.anno.method.interfaces.*;
import com.alicp.jetcache.anno.support.CacheContext;
import com.alicp.jetcache.anno.support.ConfigProvider;
import com.alicp.jetcache.anno.support.JetCacheBaseBeans;
import com.alicp.jetcache.test.anno.TestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class ProxyUtilTest {

    private ConfigProvider configProvider;
    private CacheManager cacheManager;

    @BeforeEach
    public void setup() {
        configProvider = TestUtil.createConfigProvider();
        configProvider.init();
        cacheManager = new JetCacheBaseBeans().cacheManager(configProvider);
    }

    @AfterEach
    public void stop() {
        configProvider.shutdown();
    }

    // Test class for annotation on class
    @Nested
    class AnnotationOnClassTest {


        public class C1 implements I1 {
            int count;

            @Cached
            public int count() {
                return count++;
            }

            public int countWithoutCache() {
                return count++;
            }
        }

        @Test
        public void testGetProxyByAnnotation1() {
            I1 c1 = new C1();
            I1 c2 = ProxyUtil.getProxyByAnnotation(c1, configProvider, cacheManager);
            assertNotEquals(c1.count(), c1.count());
            assertNotEquals(c1.countWithoutCache(), c1.countWithoutCache());
            assertEquals(c2.count(), c2.count());
            assertNotEquals(c2.countWithoutCache(), c2.countWithoutCache());
        }
    }

    // Test class for annotation on interface
    @Nested
    class AnnotationOnInterfaceTest {

        public class C2 implements I2 {
            int count;

            public int count() {
                return count++;
            }

            public int countWithoutCache() {
                return count++;
            }
        }

        public  class C22 implements I2 {
            int count;

            public int count() {
                return count++;
            }

            public int countWithoutCache() {
                return count++;
            }
        }

        @Test
        public void testGetProxyByAnnotation2() {
            I2 c1 = new C2();
            I2 c2 = ProxyUtil.getProxyByAnnotation(c1, configProvider, cacheManager);

            assertNotEquals(c1.count(), c1.count());
            assertNotEquals(c1.countWithoutCache(), c1.countWithoutCache());
            assertEquals(c2.count(), c2.count());
            assertNotEquals(c2.countWithoutCache(), c2.countWithoutCache());

            I2 c3 = new C22();
            I2 c4 = ProxyUtil.getProxyByAnnotation(c3, configProvider, cacheManager);
            assertEquals(c2.count(), c4.count());
        }
    }

    @Nested
    class AnnotationOnSuperInterfaceTest {

        public class C3 implements I3_2 {
            int count;

            public int count() {
                return count++;
            }

            public int countWithoutCache() {
                return count++;
            }
        }

        @Test
        public void testGetProxyByAnnotation3() {
            I3_2 c1 = new C3();
            I3_2 c2 = ProxyUtil.getProxyByAnnotation(c1, configProvider, cacheManager);
            assertNotEquals(c1.count(), c1.count());
            assertNotEquals(c1.countWithoutCache(), c1.countWithoutCache());
            assertEquals(c2.count(), c2.count());
            assertNotEquals(c2.countWithoutCache(), c2.countWithoutCache());
        }
    }

    // Test class for annotation on sub interface
    @Nested
    class AnnotationOnSubInterfaceTest {

        public class C4 implements I4_2 {
            int count;

            public int count() {
                return count++;
            }

            public int countWithoutCache() {
                return count++;
            }
        }

        @Test
        public void testGetProxyByAnnotation4() {
            I4_1 c1 = new C4();
            I4_1 c2 = ProxyUtil.getProxyByAnnotation(c1, configProvider, cacheManager);
            assertNotEquals(c1.count(), c1.count());
            assertNotEquals(c1.countWithoutCache(), c1.countWithoutCache());
            assertEquals(c2.count(), c2.count());
            assertNotEquals(c2.countWithoutCache(), c2.countWithoutCache());
        }
    }

    // Test class for annotation with enabled=false
    @Nested
    class AnnotationWithEnabledFalseTest {
        public class C5 implements I5 {
            int count;

            @Cached(enabled = false)
            public int count() {
                return count++;
            }

            public int countWithoutCache() {
                return count++;
            }
        }

        @Test
        public void testGetProxyByAnnotation5() {
            I5 c1 = new C5();
            I5 c2 = ProxyUtil.getProxyByAnnotation(c1, configProvider, cacheManager);
            assertNotEquals(c1.count(), c1.count());
            assertNotEquals(c1.countWithoutCache(), c1.countWithoutCache());
            assertNotEquals(c2.count(), c2.count());
            assertNotEquals(c2.countWithoutCache(), c2.countWithoutCache());
            CacheContext.enableCache(() -> {
                assertNotEquals(c1.count(), c1.count());
                assertNotEquals(c1.countWithoutCache(), c1.countWithoutCache());
                assertEquals(c2.count(), c2.count());
                assertNotEquals(c2.countWithoutCache(), c2.countWithoutCache());
                return null;
            });
        }
    }

    // Test class for annotation with enabled=false + EnableCache
    @Nested
    class AnnotationWithEnabledFalseAndEnableCacheTest {

        public class C6 implements I6 {
            int count;

            @EnableCache
            @Cached(enabled = false)
            public int count() {
                return count++;
            }

            public int countWithoutCache() {
                return count++;
            }
        }

        @Test
        public void testGetProxyByAnnotation6() {
            I6 c1 = new C6();
            I6 c2 = ProxyUtil.getProxyByAnnotation(c1, configProvider, cacheManager);
            assertNotEquals(c1.count(), c1.count());
            assertNotEquals(c1.countWithoutCache(), c1.countWithoutCache());
            assertEquals(c2.count(), c2.count());
            assertNotEquals(c2.countWithoutCache(), c2.countWithoutCache());
        }
    }

    // Test class for annotation with enabled=false + EnableCache (enable in caller)
    @Nested
    class AnnotationWithEnabledFalseAndEnableCacheInCallerTest {

        public class C7_1 implements I7_1 {
            int count;

            @Cached(enabled = false)
            public int count() {
                return count++;
            }

            @Override
            public int countWithoutCache() {
                return count++;
            }
        }

        public class C7_2 implements I7_2 {
            I7_1 service;

            @EnableCache
            public int count() {
                return service.count();
            }

            @EnableCache
            public int countWithoutCache() {
                return service.countWithoutCache();
            }
        }

        @Test
        public void testGetProxyByAnnotation7() {
            I7_1 c1_1 = new C7_1();
            I7_1 c1_2 = ProxyUtil.getProxyByAnnotation(c1_1, configProvider, cacheManager);

            C7_2 c2_1 = new C7_2();
            c2_1.service = c1_2;
            I7_2 c2_2 = ProxyUtil.getProxyByAnnotation(c2_1, configProvider, cacheManager);
            assertNotEquals(c2_1.count(), c2_1.count());
            assertNotEquals(c2_2.countWithoutCache(), c2_2.countWithoutCache());
            assertEquals(c2_2.count(), c2_2.count());
        }
    }

    // Test class for @CacheUpdate and @CacheInvalidate test
    @Nested
    class CacheUpdateInvalidateTest {



        public class C8 implements I8 {
            int count;
            Map<String, Integer> m = new HashMap<>();

            @Override
            public int count(String id) {
                Integer v = m.get(id);
                if (v == null) {
                    v = count++;
                }
                v++;
                m.put(id, v);
                return v;
            }

            @Override
            public void update(String theId, int value) {
                m.put(theId, value);
            }

            @Override
            public void delete(String theId) {
                m.remove(theId);
            }

            @Override
            public void update2(String theId, int value) {
                m.put(theId, value);
            }

            @Override
            public void delete2(String theId) {
                m.remove(theId);
            }

            @Override
            public int randomUpdate(String id) {
                return new Random().nextInt();
            }

            @Override
            public int randomUpdate2(String id) {
                return new Random().nextInt();
            }
        }

        @Test
        public void testGetProxyByAnnotation8() {
            I8 i8 = new C8();
            I8 i8_proxy = ProxyUtil.getProxyByAnnotation(i8, configProvider, cacheManager);

            int v1 = i8_proxy.count("K1");
            assertEquals(v1, i8_proxy.count("K1"));

            i8_proxy.delete("K1");
            int v2 = i8_proxy.count("K1");
            assertNotEquals(v1, v2);
            i8_proxy.delete2("K1");
            assertEquals(v2, i8_proxy.count("K1"));

            i8_proxy.update("K1", 200);
            assertEquals(200, i8_proxy.count("K1"));
            i8_proxy.update2("K1", 300);
            assertEquals(200, i8_proxy.count("K1"));

            assertEquals(i8_proxy.count("K1"), i8_proxy.count("K1"));
            assertNotEquals(i8_proxy.count("K1"), i8_proxy.count("K2"));

            assertEquals(i8_proxy.randomUpdate("K1"), i8_proxy.count("K1"));
            assertEquals(i8_proxy.randomUpdate2("K1"), i8_proxy.count("K1"));
        }
    }

    // Test class for @CacheRefresh test
    @Nested
    class CacheRefreshTest {

        public  class C9 implements I9 {
            int count1;
            int count2;

            public int count() {
                return count1++;
            }

            @Override
            public int count(int a, int b) {
                return a + b + count2++;
            }
        }

        @Test
        public void testGetProxyByAnnotation9() throws Exception {
            I9 beanProxy = ProxyUtil.getProxyByAnnotation(new C9(), configProvider, cacheManager);
            {
                int x1 = beanProxy.count();
                int x2 = beanProxy.count();
                assertEquals(x1, x2);
                int i = 0;
                while (true) { //auto refreshment may take some time to init
                    assertTrue(i < 10);
                    Thread.sleep(150);
                    if (x2 == beanProxy.count()) {
                        i++;
                        continue;
                    } else {
                        break;
                    }
                }
            }
            {
                int x1 = beanProxy.count(1, 2);
                int x2 = beanProxy.count(1, 200);
                assertEquals(x1, x2);
                Thread.sleep(150);
                assertEquals(x1 + 1, beanProxy.count(1, 400));
            }
        }
    }

    // Test class for @CachePenetrationProtect test
    @Nested
    class CachePenetrationProtectTest {
        public class C10 implements I10 {
            AtomicInteger count1 = new AtomicInteger(0);
            AtomicInteger count2 = new AtomicInteger(0);

            @Override
            public int count1(int p) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return count1.incrementAndGet();
            }

            @Override
            public int count2(int p) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return count2.incrementAndGet();
            }
        }

        @Test
        public void testGetProxyByAnnotation10() throws Exception {
            I10 beanProxy = ProxyUtil.getProxyByAnnotation(new C10(), configProvider, cacheManager);

            // preheat
            beanProxy.count1(1);
            beanProxy.count2(1);
            CountDownLatch latch = new CountDownLatch(20);
            for (int i = 0; i < 20; i++) {
                new Thread(() -> {
                    try {
                        int r1 = beanProxy.count1(1);
                        int r2 = beanProxy.count2(1);
                        assertEquals(r1, r2);
                    } finally {
                        latch.countDown();
                    }
                }).start();
            }
            latch.await();
        }
    }


    // Test class for @CacheExpire test
    @Nested
    class CacheExpireTest {


        @Test
        public void testGetProxyByAnnotation12() throws Exception {
            I12 beanProxy = ProxyUtil.getProxyByAnnotation(new C12(), configProvider, cacheManager);

            int x1 = beanProxy.count(1);
            int x2 = beanProxy.count(1);
            assertEquals(x1, x2);
            Thread.sleep(150);
            assertNotEquals(x1, beanProxy.count(1));
        }
    }
}
