/**
 * Created on  13-09-23 09:29
 */
package com.taobao.geek.jetcache.impl;

import com.taobao.geek.jetcache.CacheConfig;
import com.taobao.geek.jetcache.CacheProviderFactory;
import com.taobao.geek.jetcache.CacheType;
import com.taobao.geek.jetcache.Callback;
import com.taobao.geek.jetcache.support.CountClass;
import com.taobao.geek.jetcache.support.DynamicQuery;
import com.taobao.geek.jetcache.support.TestUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * @author yeli.hl
 */
public class CacheHandlerTest {

    private CacheProviderFactory cacheProviderFactory;
    private CacheConfig cacheConfig;
    private CountClass count;

    @Before
    public void setup() {
        cacheProviderFactory = TestUtil.getCacheProviderFactory();
        cacheConfig = new CacheConfig();
        count = new CountClass();
    }

    // 测试基本功能
    @Test
    public void testStaticInvoke1() throws Throwable {
        Method method = CountClass.class.getMethod("count");
        int x1, x2, x3;

        x1 = (Integer) CachedHandler.invoke(count, method, null, cacheProviderFactory, cacheConfig);
        x2 = (Integer) CachedHandler.invoke(count, method, null, cacheProviderFactory, cacheConfig);
        x3 = (Integer) CachedHandler.invoke(count, method, null, cacheProviderFactory, cacheConfig);
        Assert.assertEquals(x1, x2);
        Assert.assertEquals(x1, x3);

        method = CountClass.class.getMethod("count", int.class);
        int X1, X2, X3, X4;

        X1 = (Integer) CachedHandler.invoke(count, method, new Object[]{1000}, cacheProviderFactory, cacheConfig);
        X2 = (Integer) CachedHandler.invoke(count, method, new Object[]{2000}, cacheProviderFactory, cacheConfig);
        X3 = (Integer) CachedHandler.invoke(count, method, new Object[]{1000}, cacheProviderFactory, cacheConfig);
        X4 = (Integer) CachedHandler.invoke(count, method, new Object[]{2000}, cacheProviderFactory, cacheConfig);
        Assert.assertEquals(X1, X3);
        Assert.assertEquals(X2, X4);

        Assert.assertNotEquals(x1, X1);
        Assert.assertNotEquals(x1, X2);
    }

    // 测试基本功能
    @Test
    public void testStaticInvoke2() throws Throwable {
        Method method = CountClass.class.getMethod("count", String.class, int.class);
        int x1, x2, x3, x4, x5, x6;

        x1 = (Integer) CachedHandler.invoke(count, method, new Object[]{"aaa", 10}, cacheProviderFactory, cacheConfig);
        x2 = (Integer) CachedHandler.invoke(count, method, new Object[]{"bbb", 100}, cacheProviderFactory, cacheConfig);
        x3 = (Integer) CachedHandler.invoke(count, method, new Object[]{"ccc", 10}, cacheProviderFactory, cacheConfig);
        x4 = (Integer) CachedHandler.invoke(count, method, new Object[]{"aaa", 10}, cacheProviderFactory, cacheConfig);
        x5 = (Integer) CachedHandler.invoke(count, method, new Object[]{"bbb", 100}, cacheProviderFactory, cacheConfig);
        x6 = (Integer) CachedHandler.invoke(count, method, new Object[]{"ccc", 10}, cacheProviderFactory, cacheConfig);
        Assert.assertEquals(x1, x4);
        Assert.assertEquals(x2, x5);
        Assert.assertEquals(x3, x6);
    }

    // 测试基本功能
    @Test
    public void testStaticInvoke3() throws Throwable {
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

    private void assertEquals(DynamicQuery q1, int p1, DynamicQuery q2, int p2) throws Throwable {
        int x1, x2;
        Method method = CountClass.class.getMethod("count", DynamicQuery.class, int.class);
        x1 = (Integer) CachedHandler.invoke(count, method, new Object[]{q1, 10}, cacheProviderFactory, cacheConfig);
        x2 = (Integer) CachedHandler.invoke(count, method, new Object[]{q2, 10}, cacheProviderFactory, cacheConfig);
        Assert.assertEquals(x1, x2);
    }

    private void assertNotEquals(DynamicQuery q1, int p1, DynamicQuery q2, int p2) throws Throwable {
        int x1, x2;
        Method method = CountClass.class.getMethod("count", DynamicQuery.class, int.class);
        x1 = (Integer) CachedHandler.invoke(count, method, new Object[]{q1, p1}, cacheProviderFactory, cacheConfig);
        x2 = (Integer) CachedHandler.invoke(count, method, new Object[]{q2, p2}, cacheProviderFactory, cacheConfig);
        Assert.assertNotEquals(x1, x2);
    }

    // 测试线程上的enableCache开关
    @Test
    public void testStaticInvoke_CacheContext() throws Throwable {
        final Method method = CountClass.class.getMethod("count");
        int x1, x2, x3;

        x1 = (Integer) CachedHandler.invoke(count, method, null, cacheProviderFactory, null);
        x2 = (Integer) CachedHandler.invoke(count, method, null, cacheProviderFactory, null);
        x3 = (Integer) CachedHandler.invoke(count, method, null, cacheProviderFactory, null);
        Assert.assertTrue(x1 != x2 && x1 != x3 && x2 != x3);

        cacheConfig.setEnabled(false);
        x1 = (Integer) CachedHandler.invoke(count, method, null, cacheProviderFactory, cacheConfig);
        x2 = (Integer) CachedHandler.invoke(count, method, null, cacheProviderFactory, cacheConfig);
        x3 = (Integer) CachedHandler.invoke(count, method, null, cacheProviderFactory, cacheConfig);
        Assert.assertTrue(x1 != x2 && x1 != x3 && x2 != x3);

        CacheContextSupport.enableCache(new Callback() {
            @Override
            public void execute() throws Throwable {
                int x1 = (Integer) CachedHandler.invoke(count, method, null, cacheProviderFactory, cacheConfig);
                int x2 = (Integer) CachedHandler.invoke(count, method, null, cacheProviderFactory, cacheConfig);
                int x3 = (Integer) CachedHandler.invoke(count, method, null, cacheProviderFactory, cacheConfig);
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
        int x1 = (Integer) CachedHandler.invoke(count, method, new Object[]{1000}, cacheProviderFactory, cacheConfig);
        cacheConfig.setCacheType(CacheType.LOCAL);
        //local miss
        int x2 = (Integer) CachedHandler.invoke(count, method, new Object[]{1000}, cacheProviderFactory, cacheConfig);
        Assert.assertNotEquals(x1, x2);
        cacheConfig.setCacheType(CacheType.BOTH);
        //local hit
        x1 = (Integer) CachedHandler.invoke(count, method, new Object[]{1000}, cacheProviderFactory, cacheConfig);
        Assert.assertEquals(x1, x2);

        cacheConfig.setCacheType(CacheType.REMOTE);
        //remote put
        x1 = (Integer) CachedHandler.invoke(count, method, new Object[]{2000}, cacheProviderFactory, cacheConfig);
        cacheConfig.setCacheType(CacheType.BOTH);
        //local miss,remote hit,local put
        x2 = (Integer) CachedHandler.invoke(count, method, new Object[]{2000}, cacheProviderFactory, cacheConfig);
        Assert.assertEquals(x1, x2);
        cacheConfig.setCacheType(CacheType.LOCAL);
        //local hit
        x2 = (Integer) CachedHandler.invoke(count, method, new Object[]{2000}, cacheProviderFactory, cacheConfig);
        Assert.assertEquals(x1, x2);

        cacheConfig.setCacheType(CacheType.BOTH);
        //local put,remote put
        x1 = (Integer) CachedHandler.invoke(count, method, new Object[]{3000}, cacheProviderFactory, cacheConfig);
        //localhit
        x2 = (Integer) CachedHandler.invoke(count, method, new Object[]{3000}, cacheProviderFactory, cacheConfig);
        Assert.assertEquals(x1, x2);
        cacheConfig.setCacheType(CacheType.LOCAL);
        //local hit
        x2 = (Integer) CachedHandler.invoke(count, method, new Object[]{3000}, cacheProviderFactory, cacheConfig);
        Assert.assertEquals(x1, x2);
        cacheConfig.setCacheType(CacheType.REMOTE);
        //remote hit
        x2 = (Integer) CachedHandler.invoke(count, method, new Object[]{3000}, cacheProviderFactory, cacheConfig);
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
        HashMap<String, CacheAnnoConfig> configMap = new HashMap<String, CacheAnnoConfig>(){
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
