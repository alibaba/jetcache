package com.alicp.jetcache.anno.method.proxyutiltest;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.anno.method.ProxyUtil;
import com.alicp.jetcache.anno.method.interfaces.*;
import com.alicp.jetcache.anno.method.proxyutiltest.annotationimpl.C10;
import com.alicp.jetcache.anno.method.proxyutiltest.annotationimpl.C12;
import com.alicp.jetcache.anno.method.proxyutiltest.annotationimpl.C8;
import com.alicp.jetcache.anno.method.proxyutiltest.annotationimpl.C9;
import com.alicp.jetcache.anno.support.ConfigProvider;
import com.alicp.jetcache.anno.support.JetCacheBaseBeans;
import com.alicp.jetcache.test.anno.TestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

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


    // Test class for @CacheUpdate and @CacheInvalidate test
    @Nested
    class CacheUpdateInvalidateTest {

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
