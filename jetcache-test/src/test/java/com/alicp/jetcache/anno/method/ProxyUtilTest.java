/**
 * Created on  13-09-23 17:35
 */
package com.alicp.jetcache.anno.method;

import com.alicp.jetcache.anno.*;
import com.alicp.jetcache.anno.support.CacheContext;
import com.alicp.jetcache.anno.support.ConfigProvider;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import com.alicp.jetcache.test.anno.TestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class ProxyUtilTest {

    private GlobalCacheConfig globalCacheConfig;

    @BeforeEach
    public void setup() {
        globalCacheConfig = TestUtil.createGloableConfig(new ConfigProvider());
        globalCacheConfig.init();
    }

    @AfterEach
    public void stop() {
        globalCacheConfig.shutdown();
    }

    public interface I1 {
        int count();

        int countWithoutCache();
    }

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
    //annotation on class
    public void testGetProxyByAnnotation1() {
        I1 c1 = new C1();
        I1 c2 = ProxyUtil.getProxyByAnnotation(c1, globalCacheConfig);
        assertNotEquals(c1.count(), c1.count());
        assertNotEquals(c1.countWithoutCache(), c1.countWithoutCache());
        assertEquals(c2.count(), c2.count());
        assertNotEquals(c2.countWithoutCache(), c2.countWithoutCache());
    }

    public interface I2 {
        @Cached
        int count();

        int countWithoutCache();
    }

    public class C2 implements I2 {
        int count;

        public int count() {
            return count++;
        }

        public int countWithoutCache() {
            return count++;
        }
    }

    public class C22 implements I2 {
        int count;

        public int count() {
            return count++;
        }

        public int countWithoutCache() {
            return count++;
        }
    }

    @Test
    //annotation on intface
    public void testGetProxyByAnnotation2() {
        I2 c1 = new C2();
        I2 c2 = ProxyUtil.getProxyByAnnotation(c1, globalCacheConfig);

        assertNotEquals(c1.count(), c1.count());
        assertNotEquals(c1.countWithoutCache(), c1.countWithoutCache());
        assertEquals(c2.count(), c2.count());
        assertNotEquals(c2.countWithoutCache(), c2.countWithoutCache());

        I2 c3 = new C22();
        I2 c4 = ProxyUtil.getProxyByAnnotation(c3, globalCacheConfig);
        assertEquals(c2.count(), c4.count());
    }

    public interface I3_1 {
        @Cached
        int count();
    }

    public interface I3_2 extends I3_1 {
        int count();

        int countWithoutCache();
    }

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
    //annotation on super interface
    public void testGetProxyByAnnotation3() {
        I3_2 c1 = new C3();
        I3_2 c2 = ProxyUtil.getProxyByAnnotation(c1, globalCacheConfig);
        assertNotEquals(c1.count(), c1.count());
        assertNotEquals(c1.countWithoutCache(), c1.countWithoutCache());
        assertEquals(c2.count(), c2.count());
        assertNotEquals(c2.countWithoutCache(), c2.countWithoutCache());
    }

    public interface I4_1 {
        int count();

        int countWithoutCache();
    }

    public interface I4_2 extends I4_1 {
        @Cached
        int count();
    }

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
    //with super interface
    public void testGetProxyByAnnotation4() {
        I4_1 c1 = new C4();
        I4_1 c2 = ProxyUtil.getProxyByAnnotation(c1, globalCacheConfig);
        assertNotEquals(c1.count(), c1.count());
        assertNotEquals(c1.countWithoutCache(), c1.countWithoutCache());
        assertEquals(c2.count(), c2.count());
        assertNotEquals(c2.countWithoutCache(), c2.countWithoutCache());
    }

    public interface I5 {
        int count();

        int countWithoutCache();
    }

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
    //enabled=false
    public void testGetProxyByAnnotation5() {
        I5 c1 = new C5();
        I5 c2 = ProxyUtil.getProxyByAnnotation(c1, globalCacheConfig);
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

    public interface I6 {
        int count();

        int countWithoutCache();
    }

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
    //enabled=false+EnableCache
    public void testGetProxyByAnnotation6() {
        I6 c1 = new C6();
        I6 c2 = ProxyUtil.getProxyByAnnotation(c1, globalCacheConfig);
        assertNotEquals(c1.count(), c1.count());
        assertNotEquals(c1.countWithoutCache(), c1.countWithoutCache());
        assertEquals(c2.count(), c2.count());
        assertNotEquals(c2.countWithoutCache(), c2.countWithoutCache());
    }

    public interface I7_1 {
        int count();

        int countWithoutCache();
    }

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

    public interface I7_2 {
        int count();

        int countWithoutCache();
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
    //enabled=false+EnableCache（enable in caller）
    public void testGetProxyByAnnotation7() {
        I7_1 c1_1 = new C7_1();
        I7_1 c1_2 = ProxyUtil.getProxyByAnnotation(c1_1, globalCacheConfig);

        C7_2 c2_1 = new C7_2();
        c2_1.service = c1_2;
        I7_2 c2_2 = ProxyUtil.getProxyByAnnotation(c2_1, globalCacheConfig);
        assertNotEquals(c2_1.count(), c2_1.count());
        assertNotEquals(c2_2.countWithoutCache(), c2_2.countWithoutCache());
        assertEquals(c2_2.count(), c2_2.count());
    }

    public interface I8 {
        @Cached(name = "c1", key = "args[0]")
        int count(String id);

        @CacheUpdate(name = "c1", key = "#id", value = "args[1]")
        void update(String id, int value);

        @CacheUpdate(name = "c2", key = "args[0]", value = "args[1]")
        void update2(String id, int value);

        @CacheInvalidate(name = "c1", key = "#id")
        void delete(String id);

        @CacheInvalidate(name = "c2", key = "args[0]")
        void delete2(String id);

        @CacheUpdate(name = "c1", key = "#id", value="#result")
        int randomUpdate(String id);

        @CacheUpdate(name = "c1", key = "#id", value="result")
        int randomUpdate2(String id);
    }

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
    // @CacheUpdate and @CacheInvalidate test
    public void testGetProxyByAnnotation8() {
        I8 i8 = new C8();
        I8 i8_proxy = ProxyUtil.getProxyByAnnotation(i8, globalCacheConfig);

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

    public interface I9 {
        @Cached
        @CacheRefresh(refresh = 100, timeUnit = TimeUnit.MILLISECONDS)
        int count();

        @Cached(key = "#a", cacheType = CacheType.BOTH)
        @CacheRefresh(refresh = 100, timeUnit = TimeUnit.MILLISECONDS)
        int count(int a, int b);
    }

    public class C9 implements I9 {
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
    // refresh test
    public void testGetProxyByAnnotation9() throws Exception {
        I9 beanProxy = ProxyUtil.getProxyByAnnotation(new C9(), globalCacheConfig);
        {
            int x1 = beanProxy.count();
            int x2 = beanProxy.count();
            assertEquals(x1, x2);
            Thread.sleep(150);
            assertNotEquals(x2, beanProxy.count());
        }
        {
            int x1 = beanProxy.count(1, 2);
            int x2 = beanProxy.count(1, 200);
            assertEquals(x1, x2);
            Thread.sleep(150);
            assertEquals(x1 + 1, beanProxy.count(1, 400));
        }
    }

    public interface I10 {
        @Cached
        int count1(int p);

        @Cached
        @CachePenetrationProtect
        int count2(int p);
    }

    public class C10 implements I10 {
        int count1;
        int count2;

        @Override
        public int count1(int p) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return count1++;
        }

        @Override
        public int count2(int p) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return count2++;
        }
    }

    @Test
    // protect test
    public void testGetProxyByAnnotation10() throws Exception {
        I10 beanProxy = ProxyUtil.getProxyByAnnotation(new C10(), globalCacheConfig);

        // preheat
        beanProxy.count1(1);
        beanProxy.count2(1);

        {
            int[] x = new int[1];
            int[] y = new int[1];
            CountDownLatch countDownLatch = new CountDownLatch(2);
            new Thread(() -> {
                x[0] = beanProxy.count1(2);
                countDownLatch.countDown();
            }).start();
            new Thread(() -> {
                y[0] = beanProxy.count1(2);
                countDownLatch.countDown();
            }).start();
            countDownLatch.await();
            assertNotEquals(x[0], y[0]);
        }
        {
            int[] x = new int[1];
            int[] y = new int[1];
            CountDownLatch countDownLatch = new CountDownLatch(2);
            new Thread(() -> {
                x[0] = beanProxy.count2(2);
                countDownLatch.countDown();
            }).start();
            new Thread(() -> {
                y[0] = beanProxy.count2(2);
                countDownLatch.countDown();
            }).start();
            countDownLatch.await();
            assertEquals(x[0], y[0]);
        }
    }
}
