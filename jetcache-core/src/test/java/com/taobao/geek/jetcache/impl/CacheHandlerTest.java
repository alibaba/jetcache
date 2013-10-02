/**
 * Created on  13-09-23 09:29
 */
package com.taobao.geek.jetcache.impl;

import com.taobao.geek.jetcache.CacheConfig;
import com.taobao.geek.jetcache.CacheProviderFactory;
import com.taobao.geek.jetcache.CacheType;
import com.taobao.geek.jetcache.Callback;
import com.taobao.geek.jetcache.support.DefaultCacheMonitor;
import com.taobao.geek.jetcache.testsupport.CountClass;
import com.taobao.geek.jetcache.testsupport.DynamicQuery;
import com.taobao.geek.jetcache.testsupport.TestUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheHandlerTest {

    private CacheProviderFactory cacheProviderFactory;
    private CacheConfig cacheConfig;
    private CountClass count;
    private DefaultCacheMonitor monitor;

    @Before
    public void setup() {
        cacheProviderFactory = TestUtil.getCacheProviderFactory();
        cacheConfig = new CacheConfig();
        count = new CountClass();
        monitor = new DefaultCacheMonitor();
        cacheProviderFactory.setCacheMonitor(monitor);
    }

    @After
    public void close() {
        //System.out.println(monitor.getStatText());
    }

    private CacheInvokeContext createContext(Invoker invoker, Method method, Object[] args) {
        CacheInvokeContext c = new CacheInvokeContext();
        c.src = count;
        c.cacheProviderFactory = cacheProviderFactory;
        c.cacheConfig = cacheConfig;
        c.invoker = invoker;
        c.method = method;
        c.args = args;
        return c;
    }

    private int invoke(Method method, Object[] params) throws Throwable {
        return (Integer) CachedHandler.invoke(createContext(null, method, params));
    }

    // basic test
    @Test
    public void testStaticInvoke1() throws Throwable {
        cacheConfig.setCacheType(CacheType.REMOTE);
        testStaticInvoke1_impl();
        setup();
        cacheConfig.setCacheType(CacheType.LOCAL);
        testStaticInvoke1_impl();
        setup();
        cacheConfig.setCacheType(CacheType.BOTH);
        testStaticInvoke1_impl();
    }

    private void testStaticInvoke1_impl() throws Throwable {
        Method method = CountClass.class.getMethod("count");
        int x1, x2, x3;

        x1 = invoke(method, null);
        x2 = invoke(method, null);
        x3 = invoke(method, null);
        Assert.assertEquals(x1, x2);
        Assert.assertEquals(x1, x3);

        method = CountClass.class.getMethod("count", int.class);
        int X1, X2, X3, X4;

        X1 = invoke(method, new Object[]{1000});
        X2 = invoke(method, new Object[]{2000});
        X3 = invoke(method, new Object[]{1000});
        X4 = invoke(method, new Object[]{2000});
        Assert.assertEquals(X1, X3);
        Assert.assertEquals(X2, X4);

        Assert.assertNotEquals(x1, X1);
        Assert.assertNotEquals(x1, X2);
    }

    // basic test
    @Test
    public void testStaticInvoke2() throws Throwable {
        cacheConfig.setCacheType(CacheType.REMOTE);
        testStaticInvoke2_impl();
        setup();
        cacheConfig.setCacheType(CacheType.LOCAL);
        testStaticInvoke2_impl();
        setup();
        cacheConfig.setCacheType(CacheType.BOTH);
        testStaticInvoke2_impl();
    }

    private void testStaticInvoke2_impl() throws Throwable {
        Method method = CountClass.class.getMethod("count", String.class, int.class);
        int x1, x2, x3, x4, x5, x6;

        x1 = invoke(method, new Object[]{"aaa", 10});
        x2 = invoke(method, new Object[]{"bbb", 100});
        x3 = invoke(method, new Object[]{"ccc", 10});
        x4 = invoke(method, new Object[]{"aaa", 10});
        x5 = invoke(method, new Object[]{"bbb", 100});
        x6 = invoke(method, new Object[]{"ccc", 10});
        Assert.assertEquals(x1, x4);
        Assert.assertEquals(x2, x5);
        Assert.assertEquals(x3, x6);
    }

    // basic test
    @Test
    public void testStaticInvoke3() throws Throwable {
        cacheConfig.setCacheType(CacheType.REMOTE);
        testStaticInvoke3_impl();
        setup();
        cacheConfig.setCacheType(CacheType.LOCAL);
        testStaticInvoke3_impl();
        setup();
        cacheConfig.setCacheType(CacheType.BOTH);
        testStaticInvoke3_impl();
    }

    private void testStaticInvoke3_impl() throws Throwable {
        DynamicQuery q1 = new DynamicQuery();
        DynamicQuery q2 = new DynamicQuery();
        q2.setId(1000);
        DynamicQuery q3 = new DynamicQuery();
        q3.setId(1000);
        q3.setName("N1");
        DynamicQuery q4 = new DynamicQuery();
        q4.setId(1000);
        q4.setName("N2");
        DynamicQuery q5 = new DynamicQuery();
        q5.setId(1000);
        q5.setName("N2");
        q5.setEmail("");
        DynamicQuery q6 = new DynamicQuery();//q6=q4
        q6.setId(1000);
        q6.setName("N2");

        DynamicQuery[] querys = new DynamicQuery[]{q1, q2, q3, q4, q5, q6};
        int[] ps = new int[]{10, 9000000, 10};

        for (DynamicQuery Q1 : querys) {
            for (DynamicQuery Q2 : querys) {
                for (int P1 : ps) {
                    for (int P2 : ps) {
                        if (Q1 == Q2 && P1 == P2) {
                            assertEquals(Q1, P1, Q2, P2);
                        } else if (P1 == P2 && (Q1 == q4 || Q1 == q6) && (Q2 == q4 || Q2 == q6)) {
                            assertEquals(Q1, P1, Q2, P2);
                        } else {
                            assertNotEquals(Q1, P1, Q2, P2);
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testStaticInvokeNull() throws Throwable {
        Method method = CountClass.class.getMethod("countNull");
        Integer x1, x2, x3;
        x1 = (Integer) CachedHandler.invoke(createContext(null, method, null));
        x2 = (Integer) CachedHandler.invoke(createContext(null, method, null));
        x3 = (Integer) CachedHandler.invoke(createContext(null, method, null));
        Assert.assertNull(x1);//null, not cached
        Assert.assertNotNull(x2);
        Assert.assertNotNull(x3);//cached

        setup();
        cacheConfig.setCacheNullValue(true);
        x1 = (Integer) CachedHandler.invoke(createContext(null, method, null));
        x2 = (Integer) CachedHandler.invoke(createContext(null, method, null));
        x3 = (Integer) CachedHandler.invoke(createContext(null, method, null));
        Assert.assertNull(x1);//null,cached
        Assert.assertNull(x2);
        Assert.assertNull(x3);
    }

    private void assertEquals(DynamicQuery q1, int p1, DynamicQuery q2, int p2) throws Throwable {
        int x1, x2;
        Method method = CountClass.class.getMethod("count", DynamicQuery.class, int.class);
        x1 = invoke(method, new Object[]{q1, 10});
        x2 = invoke(method, new Object[]{q2, 10});
        Assert.assertEquals(x1, x2);
    }

    private void assertNotEquals(DynamicQuery q1, int p1, DynamicQuery q2, int p2) throws Throwable {
        int x1, x2;
        Method method = CountClass.class.getMethod("count", DynamicQuery.class, int.class);
        x1 = invoke(method, new Object[]{q1, p1});
        x2 = invoke(method, new Object[]{q2, p2});
        Assert.assertNotEquals(x1, x2);
    }

    // test enableCache
    @Test
    public void testStaticInvoke_CacheContext() throws Throwable {
        final Method method = CountClass.class.getMethod("count");
        int x1, x2, x3;

        CacheInvokeContext context = createContext(null, method, null);
        context.cacheConfig = null;
        x1 = (Integer) CachedHandler.invoke(context);
        context = createContext(null, method, null);
        context.cacheConfig = null;
        x2 = (Integer) CachedHandler.invoke(context);
        context = createContext(null, method, null);
        context.cacheConfig = null;
        x3 = (Integer) CachedHandler.invoke(context);
        Assert.assertTrue(x1 != x2 && x1 != x3 && x2 != x3);

        cacheConfig.setEnabled(false);
        x1 = invoke(method, null);
        x2 = invoke(method, null);
        x3 = invoke(method, null);
        Assert.assertTrue(x1 != x2 && x1 != x3 && x2 != x3);

        CacheContextSupport.enableCache(new Callback() {
            @Override
            public void execute() throws Throwable {
                int x1 = invoke(method, null);
                int x2 = invoke(method, null);
                int x3 = invoke(method, null);
                Assert.assertEquals(x1, x2);
                Assert.assertEquals(x1, x3);
            }
        });
    }

    @Test
    public void testStaticInvoke_BOTH() throws Throwable {
        Method method = CountClass.class.getMethod("count", int.class);

        cacheConfig.setCacheType(CacheType.REMOTE);
        //remote put
        int x1 = invoke(method, new Object[]{1000});
        cacheConfig.setCacheType(CacheType.LOCAL);
        //local miss
        int x2 = invoke(method, new Object[]{1000});
        Assert.assertNotEquals(x1, x2);
        cacheConfig.setCacheType(CacheType.BOTH);
        //local hit
        x1 = invoke(method, new Object[]{1000});
        Assert.assertEquals(x1, x2);

        cacheConfig.setCacheType(CacheType.REMOTE);
        //remote put
        x1 = invoke(method, new Object[]{2000});
        cacheConfig.setCacheType(CacheType.BOTH);
        //local miss,remote hit,local put
        x2 = invoke(method, new Object[]{2000});
        Assert.assertEquals(x1, x2);
        cacheConfig.setCacheType(CacheType.LOCAL);
        //local hit
        x2 = invoke(method, new Object[]{2000});
        Assert.assertEquals(x1, x2);

        cacheConfig.setCacheType(CacheType.BOTH);
        //local put,remote put
        x1 = invoke(method, new Object[]{3000});
        //localhit
        x2 = invoke(method, new Object[]{3000});
        Assert.assertEquals(x1, x2);
        cacheConfig.setCacheType(CacheType.LOCAL);
        //local hit
        x2 = invoke(method, new Object[]{3000});
        Assert.assertEquals(x1, x2);
        cacheConfig.setCacheType(CacheType.REMOTE);
        //remote hit
        x2 = invoke(method, new Object[]{3000});
        Assert.assertEquals(x1, x2);
    }

    @Test
    public void testInvoke1() throws Throwable {
        Method method = CountClass.class.getMethod("count");
        CachedHandler ch = new CachedHandler(count, cacheConfig, cacheProviderFactory);
        int x1 = (Integer) ch.invoke(null, method, null);
        int x2 = (Integer) ch.invoke(null, method, null);
        Assert.assertEquals(x1, x2);
    }

    @Test
    public void testInvoke2() throws Throwable {
        Method method = CountClass.class.getMethod("count");
        final CacheAnnoConfig cac = new CacheAnnoConfig();
        cac.setCacheConfig(cacheConfig);
        HashMap<String, CacheAnnoConfig> configMap = new HashMap<String, CacheAnnoConfig>() {
            @Override
            public CacheAnnoConfig get(Object key) {
                return cac;
            }
        };
        CachedHandler ch = new CachedHandler(count, configMap, cacheProviderFactory);

        int x1 = (Integer) ch.invoke(null, method, null);
        int x2 = (Integer) ch.invoke(null, method, null);
        Assert.assertEquals(x1, x2);

        cacheConfig.setEnabled(false);
        x1 = (Integer) ch.invoke(null, method, null);
        x2 = (Integer) ch.invoke(null, method, null);
        Assert.assertNotEquals(x1, x2);

        cac.setEnableCacheContext(true);
        x1 = (Integer) ch.invoke(null, method, null);
        x2 = (Integer) ch.invoke(null, method, null);
        Assert.assertEquals(x1, x2);
    }

}
