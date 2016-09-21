/**
 * Created on  13-09-22 18:46
 */
package com.alicp.jetcache.spring;

import com.alibaba.fastjson.util.IdentityHashMap;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.alicp.jetcache.anno.EnableCache;
import com.alicp.jetcache.anno.impl.CacheInvokeConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import otherpackage.OtherService;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CachePointCutTest {
    private CachePointcut pc;
    private IdentityHashMap<Method, CacheInvokeConfig> map;

    @Before
    public void setup() {
        pc = new CachePointcut(new String[]{"com.alicp.jetcache"});
        map = new IdentityHashMap<Method, CacheInvokeConfig>();
        pc.setCacheConfigMap(map);
    }

    interface I1 {
        @Cached
        int foo();
    }

    class C1 implements I1 {
        public int foo() {
            return 0;
        }
    }

    class C1_2 implements OtherService {
        public int bar() {
            return 0;
        }

        @Cached
        public int bar2(){
            return 0;
        }
    }

    @Test
    public void testMatches1() throws Exception {
        Assert.assertTrue(pc.matches(C1.class));
        Assert.assertTrue(pc.matches(I1.class));
        Assert.assertTrue(pc.matches(C1_2.class));
        Method m1 = I1.class.getMethod("foo");
        Method m2 = C1.class.getMethod("foo");
        Method m3 = OtherService.class.getMethod("bar");
        Method m4 = C1_2.class.getMethod("bar");
        Assert.assertTrue(pc.matches(m1, C1.class));
        Assert.assertTrue(pc.matches(m2, C1.class));
        Assert.assertTrue(pc.matches(m1, I1.class));
        Assert.assertTrue(pc.matches(m2, I1.class));
        Assert.assertFalse(pc.matches(m3, OtherService.class));
        Assert.assertFalse(pc.matches(m4, OtherService.class));
        Assert.assertFalse(pc.matches(m3, C1_2.class));
        Assert.assertFalse(pc.matches(m4, C1_2.class));
        Assert.assertTrue(pc.matches(C1_2.class.getMethod("bar2"), C1_2.class));

        Assert.assertFalse(map.get(m1).isEnableCacheContext());
        Assert.assertFalse(map.get(m2).isEnableCacheContext());
        Assert.assertNotNull(map.get(m1).getCacheConfig());
        Assert.assertNotNull(map.get(m2).getCacheConfig());

        Object o1 = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{I1.class}, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return null;
            }
        });
        Assert.assertTrue(pc.matches(m1, o1.getClass()));
        Assert.assertTrue(pc.matches(m2, o1.getClass()));
        Assert.assertTrue(pc.matches(o1.getClass().getMethod("foo"), o1.getClass()));
    }


    interface I2 {
        int foo();
    }

    class C2 implements I2 {
        @Cached
        public int foo() {
            return 0;
        }
    }

    @Test
    public void testMatches2() throws Exception {
        Method m1 = I2.class.getMethod("foo");
        Method m2 = C2.class.getMethod("foo");
        Assert.assertTrue(pc.matches(m1, C2.class));
        Assert.assertTrue(pc.matches(m2, C2.class));
        Assert.assertTrue(pc.matches(m1, I2.class));
        Assert.assertTrue(pc.matches(m2, I2.class));

        Assert.assertFalse(map.get(m1).isEnableCacheContext());
        Assert.assertFalse(map.get(m2).isEnableCacheContext());
        Assert.assertNotNull(map.get(m1).getCacheConfig());
        Assert.assertNotNull(map.get(m2).getCacheConfig());
    }

    interface I3_Parent {
        @EnableCache
        @Cached(enabled = false, area = "A1", expire = 1, cacheType = CacheType.BOTH, localLimit = 2, version = 10)
        int foo();
    }

    interface I3 extends I3_Parent {
        int foo();
    }

    class C3 implements I3 {
        public int foo() {
            return 0;
        }
    }

    @Test
    public void testMatches3() throws Exception {
        Method m1 = I3_Parent.class.getMethod("foo");
        Method m2 = I3.class.getMethod("foo");
        Method m3 = C3.class.getMethod("foo");
        Assert.assertTrue(pc.matches(m1, C3.class));
        Assert.assertTrue(pc.matches(m2, C3.class));
        Assert.assertTrue(pc.matches(m3, C3.class));

        Assert.assertTrue(map.get(m1).isEnableCacheContext());
        Assert.assertTrue(map.get(m2).isEnableCacheContext());
        Assert.assertTrue(map.get(m3).isEnableCacheContext());
        Assert.assertEquals("A1", map.get(m1).getCacheConfig().getArea());
        Assert.assertEquals(false, map.get(m1).getCacheConfig().isEnabled());
        Assert.assertEquals(1, map.get(m1).getCacheConfig().getExpire());
        Assert.assertEquals(CacheType.BOTH, map.get(m1).getCacheConfig().getCacheType());
        Assert.assertEquals(2, map.get(m1).getCacheConfig().getLocalLimit());
        Assert.assertEquals(10, map.get(m1).getCacheConfig().getVersion());

    }


    interface I4 {
        @Cached
        int foo();
    }

    interface I4_Sub extends I4{
    }

    class C4 implements I4_Sub {
        public int foo() {
            return 0;
        }
    }

    @Test
    public void testMatches4() throws Exception {
        Method m1 = I4.class.getMethod("foo");
        Method m2 = C4.class.getMethod("foo");
        Assert.assertTrue(pc.matches(m1, C4.class));
        Assert.assertTrue(pc.matches(m2, C4.class));
        Assert.assertTrue(pc.matches(m1, I4.class));
        Assert.assertTrue(pc.matches(m2, I4.class));

        Assert.assertFalse(map.get(m1).isEnableCacheContext());
        Assert.assertFalse(map.get(m2).isEnableCacheContext());
        Assert.assertNotNull(map.get(m1).getCacheConfig());
        Assert.assertNotNull(map.get(m2).getCacheConfig());

    }
}
