/**
 * Created on  13-09-23 09:29
 */
package com.alicp.jetcache.anno.method;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheConsts;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.KeyConvertor;
import com.alicp.jetcache.anno.support.CacheAnnoConfig;
import com.alicp.jetcache.anno.support.CacheContext;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import com.alicp.jetcache.embedded.LinkedHashMapCacheBuilder;
import com.alicp.jetcache.support.FastjsonKeyConvertor;
import com.alicp.jetcache.test.support.DynamicQuery;
import com.alicp.jetcache.testsupport.CountClass;
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

    private GlobalCacheConfig globalCacheConfig;
    private CacheAnnoConfig cacheAnnoConfig;
    private CacheInvokeConfig cacheInvokeConfig;
    private CountClass count;
    private Cache cache;

    @Before
    public void setup() {
        globalCacheConfig = new GlobalCacheConfig();
        globalCacheConfig.init();
        cache = LinkedHashMapCacheBuilder.createLinkedHashMapCacheBuilder()
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .buildCache();

        cacheAnnoConfig = new CacheAnnoConfig();
        cacheAnnoConfig.setArea(CacheConsts.DEFAULT_AREA);
        cacheAnnoConfig.setName(CacheConsts.UNDEFINED_STRING);
        cacheAnnoConfig.setEnabled(CacheConsts.DEFAULT_ENABLED);
        cacheAnnoConfig.setExpire(CacheConsts.DEFAULT_EXPIRE);
        cacheAnnoConfig.setCacheType(CacheType.REMOTE);
        cacheAnnoConfig.setLocalLimit(CacheConsts.DEFAULT_LOCAL_LIMIT);
        cacheAnnoConfig.setCacheNullValue(CacheConsts.DEFAULT_CACHE_NULL_VALUE);
        cacheAnnoConfig.setCondition(CacheConsts.UNDEFINED_STRING);
        cacheAnnoConfig.setUnless(CacheConsts.UNDEFINED_STRING);
        cacheAnnoConfig.setSerialPolicy(CacheConsts.DEFAULT_SERIAL_POLICY);
        cacheAnnoConfig.setKeyConvertor(KeyConvertor.FASTJSON);


        cacheInvokeConfig = new CacheInvokeConfig();
        cacheInvokeConfig.setCacheAnnoConfig(cacheAnnoConfig);
        count = new CountClass();
    }

    @After
    public void stop() {
        globalCacheConfig.shutdown();
    }

    private CacheInvokeContext createContext(Invoker invoker, Method method, Object[] args) {
        CacheInvokeContext c = globalCacheConfig.getCacheContext().createCacheInvokeContext();
        c.cacheInvokeConfig = cacheInvokeConfig;
        cacheInvokeConfig.setCacheAnnoConfig(cacheAnnoConfig);
        c.invoker = invoker;
        c.method = method;
        c.args = args;
        c.setCacheFunction((n) -> cache);
        return c;
    }

    private Integer invoke(Method method, Object[] params) throws Throwable {
        return (Integer) CacheHandler.invoke(createContext(() -> method.invoke(count, params), method, params));
    }

    // basic test
    @Test
    public void testStaticInvoke1() throws Throwable {
        Method method = CountClass.class.getMethod("count");
        int x1, x2, x3;
        method.invoke(count);

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

    }

    // basic test
    @Test
    public void testStaticInvoke2() throws Throwable {
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

        cacheAnnoConfig.setCacheNullValue(false);
        x1 = invoke(method, null);//null, not cached
        x2 = invoke(method, null);//not null, so cached
        x3 = invoke(method, null);//hit cache
        Assert.assertNull(x1);
        Assert.assertNotNull(x2);
        Assert.assertNotNull(x3);
        Assert.assertEquals(x2, x3);

        setup();
        cacheAnnoConfig.setCacheNullValue(true);
        x1 = invoke(method, null); //null,cached
        x2 = invoke(method, null);
        x3 = invoke(method, null);
        Assert.assertNull(x1);
        Assert.assertNull(x2);
        Assert.assertNull(x3);

        cacheAnnoConfig.setCacheNullValue(false);
        x1 = invoke(method, null);//cached value is null, invoke, cached
        x2 = invoke(method, null);
        x3 = invoke(method, null);
        Assert.assertNotNull(x1);
        Assert.assertNotNull(x2);
        Assert.assertNotNull(x3);
        Assert.assertEquals(x1, x2);
        Assert.assertEquals(x2, x3);
    }

    @Test
    public void testStaticInvokeCondition() throws Throwable {
        Method method = CountClass.class.getMethod("count", int.class);
        int x1, x2;
        cacheAnnoConfig.setCondition("mvel{args[0]>10}");
        cacheInvokeConfig.init();
        x1 = invoke(method, new Object[]{10});
        x2 = invoke(method, new Object[]{10});
        Assert.assertNotEquals(x1, x2);
        x1 = invoke(method, new Object[]{11});
        x2 = invoke(method, new Object[]{11});
        Assert.assertEquals(x1, x2);
    }

    @Test
    public void testStaticInvokeUnless() throws Throwable {
        Method method = CountClass.class.getMethod("count");
        int x1, x2, x3, x4;
        cacheAnnoConfig.setUnless("mvel{result%2==1}");
        cacheInvokeConfig.init();
        x1 = invoke(method, null);//return 0, unless=false, so cached
        x2 = invoke(method, null);//cache hit
        Assert.assertEquals(x1, x2);
        cacheAnnoConfig.setUnless("mvel{result%2==0}");
        cacheInvokeConfig.init();
        x3 = invoke(method, null);//cache hit(0),unless=true,invoke and return 1, and then cached
        x4 = invoke(method, null);//cache hit(1)
        Assert.assertEquals(x3, x4);

        Assert.assertNotEquals(x3, x1);
    }

    @Test
    public void testStaticInvokeUnlessAndNull() throws Throwable {
        Method method = CountClass.class.getMethod("countNull");

        cacheAnnoConfig.setCacheNullValue(false);
        cacheAnnoConfig.setUnless("mvel{result==0}");
        cacheInvokeConfig.init();
        Assert.assertNull(invoke(method, null));//null, not cached
        Assert.assertEquals(0, invoke(method, null).longValue());//0, not cached
        Assert.assertEquals(1, invoke(method, null).longValue());//1, cache
        Assert.assertEquals(1, invoke(method, null).longValue());//cache hit

        cacheAnnoConfig.setUnless("mvel{result==1}");
        cacheInvokeConfig.init();
        Assert.assertEquals(2, invoke(method, null).longValue());
        Assert.assertEquals(2, invoke(method, null).longValue());

        count = new CountClass();
        cacheAnnoConfig.setUnless("mvel{result==2}");
        cacheInvokeConfig.init();
        Assert.assertNull(invoke(method, null));
        Assert.assertEquals(0, invoke(method, null).longValue());//0, cached
        Assert.assertEquals(0, invoke(method, null).longValue());//cache hit

        cacheAnnoConfig.setCacheNullValue(true);
        count = new CountClass();
        cacheAnnoConfig.setUnless("mvel{result==0}");
        cacheInvokeConfig.init();
        Assert.assertNull(invoke(method, null));
        Assert.assertNull(invoke(method, null));
    }

    private void assertEquals(DynamicQuery q1, int p1, DynamicQuery q2, int p2) throws Throwable {
        int x1, x2;
        Method method = CountClass.class.getMethod("count", DynamicQuery.class, int.class);
        x1 = invoke(method, new Object[]{q1, p1});
        x2 = invoke(method, new Object[]{q2, p2});
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
        Invoker invoker = () -> method.invoke(count);

        CacheInvokeContext context = createContext(invoker, method, null);
        cacheAnnoConfig.setEnabled(false);
        x1 = (Integer) CacheHandler.invoke(context);
        context = createContext(invoker, method, null);
        context.cacheInvokeConfig.setCacheAnnoConfig(null);
        x2 = (Integer) CacheHandler.invoke(context);
        context = createContext(invoker, method, null);
        context.cacheInvokeConfig.setCacheAnnoConfig(null);
        x3 = (Integer) CacheHandler.invoke(context);
        Assert.assertTrue(x1 != x2 && x1 != x3 && x2 != x3);

        cacheAnnoConfig.setEnabled(false);
        x1 = invoke(method, null);
        x2 = invoke(method, null);
        x3 = invoke(method, null);
        Assert.assertTrue(x1 != x2 && x1 != x3 && x2 != x3);

        cacheAnnoConfig.setEnabled(false);
        CacheContext.enableCache(() -> {
            try {
                int xx1 = invoke(method, null);
                int xx2 = invoke(method, null);
                int xx3 = invoke(method, null);
                Assert.assertEquals(xx1, xx2);
                Assert.assertEquals(xx1, xx3);
            } catch (Throwable e) {
                Assert.fail();
            }
            return null;
        });

        cacheAnnoConfig.setEnabled(false);
        cacheInvokeConfig.setEnableCacheContext(true);
        x1 = invoke(method, null);
        x2 = invoke(method, null);
        x3 = invoke(method, null);
        Assert.assertEquals(x1, x2);
        Assert.assertEquals(x1, x3);
    }

    @Test
    public void testInvoke1() throws Throwable {
        Method method = CountClass.class.getMethod("count");
        Invoker invoker = () -> method.invoke(count);
        CacheHandler ch = new CacheHandler(count, cacheInvokeConfig, () -> createContext(invoker, method, null), null);
        int x1 = (Integer) ch.invoke(null, method, null);
        int x2 = (Integer) ch.invoke(null, method, null);
        Assert.assertEquals(x1, x2);
    }

    @Test
    public void testInvoke2() throws Throwable {
        Method method = CountClass.class.getMethod("count");
        final CacheInvokeConfig cac = new CacheInvokeConfig();
        cac.setCacheAnnoConfig(cacheAnnoConfig);
        HashMap<String, CacheInvokeConfig> configMap = new HashMap<String, CacheInvokeConfig>() {
            @Override
            public CacheInvokeConfig get(Object key) {
                return cac;
            }
        };
        Invoker invoker = () -> method.invoke(count);
        CacheHandler ch = new CacheHandler(count, configMap, () -> createContext(invoker, method, null), null);

        int x1 = (Integer) ch.invoke(null, method, null);
        int x2 = (Integer) ch.invoke(null, method, null);
        Assert.assertEquals(x1, x2);

        cacheAnnoConfig.setEnabled(false);
        x1 = (Integer) ch.invoke(null, method, null);
        x2 = (Integer) ch.invoke(null, method, null);
        Assert.assertNotEquals(x1, x2);

        cac.setEnableCacheContext(true);
        x1 = (Integer) ch.invoke(null, method, null);
        x2 = (Integer) ch.invoke(null, method, null);
        Assert.assertEquals(x1, x2);
    }

}
