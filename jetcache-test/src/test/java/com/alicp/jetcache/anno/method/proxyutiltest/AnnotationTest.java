package com.alicp.jetcache.anno.method.proxyutiltest;

import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.anno.method.ProxyUtil;
import com.alicp.jetcache.anno.method.interfaces.*;
import com.alicp.jetcache.anno.method.proxyutiltest.annotationimpl.*;
import com.alicp.jetcache.anno.support.CacheContext;
import com.alicp.jetcache.anno.support.ConfigProvider;
import com.alicp.jetcache.anno.support.JetCacheBaseBeans;
import com.alicp.jetcache.test.anno.TestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

public class AnnotationTest {
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

    @Nested
    class AnnotationOnSuperInterfaceTest {
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

    @Nested
    class AnnotationOnSubInterfaceTest {
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

    @Nested
    class AnnotationWithEnabledFalseTest {

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

    @Nested
    class AnnotationWithEnabledFalseAndEnableCacheTest {
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

    @Nested
    class AnnotationWithEnabledFalseAndEnableCacheInCallerTest {


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



