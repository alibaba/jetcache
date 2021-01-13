/**
 * Created on  13-09-23 09:29
 */
package com.alicp.jetcache.anno.method;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheConsts;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.KeyConvertor;
import com.alicp.jetcache.anno.support.*;
import com.alicp.jetcache.embedded.LinkedHashMapCacheBuilder;
import com.alicp.jetcache.support.FastjsonKeyConvertor;
import com.alicp.jetcache.test.support.DynamicQuery;
import com.alicp.jetcache.testsupport.CountClass;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class CacheHandlerTest {

    private ConfigProvider configProvider;
    private CachedAnnoConfig cachedAnnoConfig;
    private CacheInvokeConfig cacheInvokeConfig;
    private CountClass count;
    private Cache cache;
    private ConfigMap configMap;

    @BeforeEach
    public void setup() {
        GlobalCacheConfig globalCacheConfig = new GlobalCacheConfig();
        configProvider = new ConfigProvider();
        configProvider.setGlobalCacheConfig(globalCacheConfig);
        configProvider.init();
        cache = LinkedHashMapCacheBuilder.createLinkedHashMapCacheBuilder()
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .buildCache();

        cachedAnnoConfig = new CachedAnnoConfig();
        cachedAnnoConfig.setArea(CacheConsts.DEFAULT_AREA);
        cachedAnnoConfig.setName("myCacheName");
        cachedAnnoConfig.setEnabled(CacheConsts.DEFAULT_ENABLED);
        cachedAnnoConfig.setTimeUnit(TimeUnit.SECONDS);
        cachedAnnoConfig.setExpire(100);
        cachedAnnoConfig.setCacheType(CacheType.REMOTE);
        cachedAnnoConfig.setLocalLimit(CacheConsts.DEFAULT_LOCAL_LIMIT);
        cachedAnnoConfig.setCacheNullValue(CacheConsts.DEFAULT_CACHE_NULL_VALUE);
        cachedAnnoConfig.setCondition(CacheConsts.UNDEFINED_STRING);
        cachedAnnoConfig.setPostCondition(CacheConsts.UNDEFINED_STRING);
        cachedAnnoConfig.setSerialPolicy(CacheConsts.DEFAULT_SERIAL_POLICY);
        cachedAnnoConfig.setKeyConvertor(KeyConvertor.FASTJSON);
        cachedAnnoConfig.setKey(CacheConsts.UNDEFINED_STRING);


        cacheInvokeConfig = new CacheInvokeConfig();
        cacheInvokeConfig.setCachedAnnoConfig(cachedAnnoConfig);

        configMap = new ConfigMap();

        count = new CountClass();
    }

    @AfterEach
    public void tearDown() {
        configProvider.shutdown();
    }


    private CacheInvokeContext createCachedInvokeContext(Invoker invoker, Method method, Object[] args) {
        CacheInvokeContext c = configProvider.getCacheContext().createCacheInvokeContext(configMap);
        c.setCacheInvokeConfig(cacheInvokeConfig);
        cacheInvokeConfig.setCachedAnnoConfig(cachedAnnoConfig);
        c.setInvoker(invoker);
        c.setMethod(method);
        c.setArgs(args);
        c.setCacheFunction((a, b) -> cache);
        return c;
    }

    private Integer invokeQuery(Method method, Object[] params) throws Throwable {
        return (Integer) CacheHandler.invoke(createCachedInvokeContext(() -> method.invoke(count, params), method, params));
    }

    // basic test
    @Test
    public void testStaticInvoke1() throws Throwable {
        Method method = CountClass.class.getMethod("count");
        cachedAnnoConfig.setDefineMethod(method);
        int x1, x2, x3;
        method.invoke(count);

        x1 = invokeQuery(method, null);
        x2 = invokeQuery(method, null);
        x3 = invokeQuery(method, null);
        assertEquals(x1, x2);
        assertEquals(x1, x3);

        method = CountClass.class.getMethod("count", int.class);
        int X1, X2, X3, X4;

        X1 = invokeQuery(method, new Object[]{1000});
        X2 = invokeQuery(method, new Object[]{2000});
        X3 = invokeQuery(method, new Object[]{1000});
        X4 = invokeQuery(method, new Object[]{2000});
        assertEquals(X1, X3);
        assertEquals(X2, X4);

    }

    // basic test
    @Test
    public void testStaticInvoke2() throws Throwable {
        Method method = CountClass.class.getMethod("count", String.class, int.class);
        cachedAnnoConfig.setDefineMethod(method);
        int x1, x2, x3, x4, x5, x6;

        x1 = invokeQuery(method, new Object[]{"aaa", 10});
        x2 = invokeQuery(method, new Object[]{"bbb", 100});
        x3 = invokeQuery(method, new Object[]{"ccc", 10});
        x4 = invokeQuery(method, new Object[]{"aaa", 10});
        x5 = invokeQuery(method, new Object[]{"bbb", 100});
        x6 = invokeQuery(method, new Object[]{"ccc", 10});
        assertEquals(x1, x4);
        assertEquals(x2, x5);
        assertEquals(x3, x6);
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
                            assertResultEquals(Q1, P1, Q2, P2);
                        } else if (P1 == P2 && (Q1 == q4 || Q1 == q6) && (Q2 == q4 || Q2 == q6)) {
                            assertResultEquals(Q1, P1, Q2, P2);
                        } else {
                            assertResultNotEquals(Q1, P1, Q2, P2);
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testStaticInvokeNull() throws Throwable {
        Method method = CountClass.class.getMethod("countNull");
        cachedAnnoConfig.setDefineMethod(method);
        Integer x1, x2, x3;

        cache.config().setCacheNullValue(false);
        x1 = invokeQuery(method, null);//null, not cached
        x2 = invokeQuery(method, null);//not null, so cached
        x3 = invokeQuery(method, null);//hit cache
        assertNull(x1);
        assertNotNull(x2);
        assertNotNull(x3);
        assertEquals(x2, x3);

        setup();
        cache.config().setCacheNullValue(true);
        x1 = invokeQuery(method, null); //null,cached
        x2 = invokeQuery(method, null);
        x3 = invokeQuery(method, null);
        assertNull(x1);
        assertNull(x2);
        assertNull(x3);
    }

    @Test
    public void testStaticInvokeCondition() throws Throwable {
        Method method = CountClass.class.getMethod("count", int.class);
        cachedAnnoConfig.setDefineMethod(method);
        int x1, x2;
        cachedAnnoConfig.setCondition("mvel{args[0]>10}");
        x1 = invokeQuery(method, new Object[]{10});
        x2 = invokeQuery(method, new Object[]{10});
        assertNotEquals(x1, x2);
        x1 = invokeQuery(method, new Object[]{11});
        x2 = invokeQuery(method, new Object[]{11});
        assertEquals(x1, x2);
    }

    @Test
    public void testStaticInvokePostCondition() throws Throwable {
        Method method = CountClass.class.getMethod("count");
        cachedAnnoConfig.setDefineMethod(method);
        int x1, x2, x3;
        cachedAnnoConfig.setPostCondition("mvel{result%2==1}");
        cacheInvokeConfig.getCachedAnnoConfig().setPostConditionEvaluator(null);
        x1 = invokeQuery(method, null);//return 0, postCondition=false, so not cached
        x2 = invokeQuery(method, null);//return 1, postCondition=true, so cached
        x3 = invokeQuery(method, null);//cache hit
        assertNotEquals(x1, x2);
        assertEquals(x2, x3);
    }

    private void assertResultEquals(DynamicQuery q1, int p1, DynamicQuery q2, int p2) throws Throwable {
        int x1, x2;
        Method method = CountClass.class.getMethod("count", DynamicQuery.class, int.class);
        cachedAnnoConfig.setDefineMethod(method);
        x1 = invokeQuery(method, new Object[]{q1, p1});
        x2 = invokeQuery(method, new Object[]{q2, p2});
        assertEquals(x1, x2);
    }

    private void assertResultNotEquals(DynamicQuery q1, int p1, DynamicQuery q2, int p2) throws Throwable {
        int x1, x2;
        Method method = CountClass.class.getMethod("count", DynamicQuery.class, int.class);
        cachedAnnoConfig.setDefineMethod(method);
        x1 = invokeQuery(method, new Object[]{q1, p1});
        x2 = invokeQuery(method, new Object[]{q2, p2});
        assertNotEquals(x1, x2);
    }

    // test enableCache
    @Test
    public void testStaticInvoke_CacheContext() throws Throwable {
        final Method method = CountClass.class.getMethod("count");
        int x1, x2, x3;
        Invoker invoker = () -> method.invoke(count);

        CacheInvokeContext context = createCachedInvokeContext(invoker, method, null);
        cachedAnnoConfig.setEnabled(false);
        x1 = (Integer) CacheHandler.invoke(context);
        context = createCachedInvokeContext(invoker, method, null);
        context.getCacheInvokeConfig().setCachedAnnoConfig(null);
        x2 = (Integer) CacheHandler.invoke(context);
        context = createCachedInvokeContext(invoker, method, null);
        context.getCacheInvokeConfig().setCachedAnnoConfig(null);
        x3 = (Integer) CacheHandler.invoke(context);
        assertTrue(x1 != x2 && x1 != x3 && x2 != x3);

        cachedAnnoConfig.setEnabled(false);
        x1 = invokeQuery(method, null);
        x2 = invokeQuery(method, null);
        x3 = invokeQuery(method, null);
        assertTrue(x1 != x2 && x1 != x3 && x2 != x3);

        cachedAnnoConfig.setEnabled(false);
        CacheContext.enableCache(() -> {
            try {
                int xx1 = invokeQuery(method, null);
                int xx2 = invokeQuery(method, null);
                int xx3 = invokeQuery(method, null);
                assertEquals(xx1, xx2);
                assertEquals(xx1, xx3);
            } catch (Throwable e) {
                fail(e);
            }
            return null;
        });

        cachedAnnoConfig.setEnabled(false);
        cacheInvokeConfig.setEnableCacheContext(true);
        x1 = invokeQuery(method, null);
        x2 = invokeQuery(method, null);
        x3 = invokeQuery(method, null);
        assertEquals(x1, x2);
        assertEquals(x1, x3);
    }

    @Test
    public void testInstanceInvoke() throws Throwable {
        Method method = CountClass.class.getMethod("count");
        cachedAnnoConfig.setDefineMethod(method);
        final CacheInvokeConfig cac = new CacheInvokeConfig();
        cac.setCachedAnnoConfig(cachedAnnoConfig);
        ConfigMap configMap = new ConfigMap() {
            @Override
            public CacheInvokeConfig getByMethodInfo(String key) {
                return cac;
            }
        };
        Invoker invoker = () -> method.invoke(count);
        CacheHandler ch = new CacheHandler(count, configMap, () -> createCachedInvokeContext(invoker, method, null), null);

        int x1 = (Integer) ch.invoke(null, method, null);
        int x2 = (Integer) ch.invoke(null, method, null);
        assertEquals(x1, x2);

        cachedAnnoConfig.setEnabled(false);
        x1 = (Integer) ch.invoke(null, method, null);
        x2 = (Integer) ch.invoke(null, method, null);
        assertNotEquals(x1, x2);

        cac.setEnableCacheContext(true);
        x1 = (Integer) ch.invoke(null, method, null);
        x2 = (Integer) ch.invoke(null, method, null);
        assertEquals(x1, x2);
    }

}
