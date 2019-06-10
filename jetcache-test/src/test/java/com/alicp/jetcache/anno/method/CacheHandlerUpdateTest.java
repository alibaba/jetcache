/**
 * Created on 2018/5/11.
 */
package com.alicp.jetcache.anno.method;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheConsts;
import com.alicp.jetcache.anno.support.CacheUpdateAnnoConfig;
import com.alicp.jetcache.anno.support.ConfigMap;
import com.alicp.jetcache.anno.support.ConfigProvider;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import com.alicp.jetcache.embedded.LinkedHashMapCacheBuilder;
import com.alicp.jetcache.support.FastjsonKeyConvertor;
import com.alicp.jetcache.testsupport.CountClass;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class CacheHandlerUpdateTest {
    private ConfigProvider configProvider;
    private CacheInvokeConfig cacheInvokeConfig;
    private CountClass count;
    private Cache cache;
    private ConfigMap configMap;
    private CacheUpdateAnnoConfig updateAnnoConfig;
    private CacheInvokeContext cacheInvokeContext;

    @BeforeEach
    public void setup() throws Exception {
        GlobalCacheConfig globalCacheConfig = new GlobalCacheConfig();
        configProvider = new ConfigProvider();
        configProvider.setGlobalCacheConfig(globalCacheConfig);
        configProvider.init();
        cache = LinkedHashMapCacheBuilder.createLinkedHashMapCacheBuilder()
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .buildCache();


        cacheInvokeConfig = new CacheInvokeConfig();

        configMap = new ConfigMap();

        count = new CountClass();

        Method method = CountClass.class.getMethod("update", String.class, int.class);
        cacheInvokeContext = configProvider.getCacheContext().createCacheInvokeContext(configMap);
        cacheInvokeContext.setCacheInvokeConfig(cacheInvokeConfig);
        updateAnnoConfig = new CacheUpdateAnnoConfig();
        updateAnnoConfig.setCondition(CacheConsts.UNDEFINED_STRING);
        updateAnnoConfig.setDefineMethod(method);
        cacheInvokeConfig.setUpdateAnnoConfig(updateAnnoConfig);

        updateAnnoConfig.setKey("args[0]");
        updateAnnoConfig.setValue("args[1]");
        cacheInvokeContext.setMethod(method);
        cacheInvokeContext.setArgs(new Object[]{"K1", 1000});
        cacheInvokeContext.setInvoker(() -> cacheInvokeContext.getMethod().invoke(count, cacheInvokeContext.getArgs()));
        cacheInvokeContext.setCacheFunction((a, b) -> cache);
    }

    @AfterEach
    public void tearDown() {
        configProvider.shutdown();
    }


    @Test
    public void testUpdate() throws Throwable {
        cache.put("K1", "V");
        CacheHandler.invoke(cacheInvokeContext);
        assertEquals(1000, cache.get("K1"));
    }


    @Test
    public void testConditionTrue() throws Throwable {
        cache.put("K1", "V");
        updateAnnoConfig.setCondition("args[1]==1000");
        CacheHandler.invoke(cacheInvokeContext);
        assertEquals(1000, cache.get("K1"));
    }

    @Test
    public void testConditionFalse() throws Throwable {
        cache.put("K1", "V");
        updateAnnoConfig.setCondition("args[1]!=1000");
        CacheHandler.invoke(cacheInvokeContext);
        assertEquals("V", cache.get("K1"));

    }

    @Test
    public void testBadCondition() throws Throwable {
        cache.put("K1", "V");
        updateAnnoConfig.setCondition("bad condition");
        CacheHandler.invoke(cacheInvokeContext);
        assertEquals("V", cache.get("K1"));
    }

    @Test
    public void testBadKey() throws Throwable {
        cache.put("K1", "V");
        updateAnnoConfig.setKey("bad key script");
        CacheHandler.invoke(cacheInvokeContext);
        assertEquals("V", cache.get("K1"));
    }

    @Test
    public void testBadValue() throws Throwable {
        cache.put("K1", "V");
        updateAnnoConfig.setValue("bad value script");
        CacheHandler.invoke(cacheInvokeContext);
        assertEquals("V", cache.get("K1"));
    }

    static class TestMulti {
        public void update(String keys, int[] values) {
        }

        public void update(String[] keys, int values) {
        }

        public void update(String[] keys, int[] values) {
        }
    }

    @Test
    public void testMulti() throws Throwable {
        {
            Method method = TestMulti.class.getMethod("update", String[].class, int[].class);
            updateAnnoConfig.setDefineMethod(method);
            updateAnnoConfig.setKey("args[0]");
            updateAnnoConfig.setValue("args[1]");
            cacheInvokeContext.setMethod(method);
            cacheInvokeContext.setArgs(new Object[]{new String[]{"K1", "K2"}, new int[]{10, 20}});
            cacheInvokeContext.setInvoker(() -> method.invoke(new TestMulti(), cacheInvokeContext.getArgs()));

            cache.put("K1", 1);
            cache.put("K2", 2);
            CacheHandler.invoke(cacheInvokeContext);
            assertEquals(1, cache.get("K1"));
            assertEquals(2, cache.get("K2"));

            updateAnnoConfig.setMulti(true);

            cacheInvokeContext.setArgs(new Object[]{null, new int[]{10, 20}});
            CacheHandler.invoke(cacheInvokeContext);
            assertEquals(1, cache.get("K1"));
            assertEquals(2, cache.get("K2"));

            cacheInvokeContext.setArgs(new Object[]{new String[]{"K1", "K2"}, null});
            CacheHandler.invoke(cacheInvokeContext);
            assertEquals(1, cache.get("K1"));
            assertEquals(2, cache.get("K2"));

            cacheInvokeContext.setArgs(new Object[]{new String[]{"K1", "K2"}, new int[]{10, 20}});
            CacheHandler.invoke(cacheInvokeContext);
            assertEquals(10, cache.get("K1"));
            assertEquals(20, cache.get("K2"));
        }
        {
            Method method = TestMulti.class.getMethod("update", String.class, int[].class);
            updateAnnoConfig.setDefineMethod(method);
            updateAnnoConfig.setKey("args[0]");
            updateAnnoConfig.setValue("args[1]");
            cacheInvokeContext.setMethod(method);
            cacheInvokeContext.setArgs(new Object[]{"K1", new int[]{10, 20}});
            cacheInvokeContext.setInvoker(() -> method.invoke(new TestMulti(), cacheInvokeContext.getArgs()));

            cache.put("K1", 1);
            cache.put("K2", 2);
            updateAnnoConfig.setMulti(true);
            CacheHandler.invoke(cacheInvokeContext);
            assertEquals(1, cache.get("K1"));
            assertEquals(2, cache.get("K2"));
        }
        {
            Method method = TestMulti.class.getMethod("update", String[].class, int.class);
            updateAnnoConfig.setDefineMethod(method);
            updateAnnoConfig.setKey("args[0]");
            updateAnnoConfig.setValue("args[1]");
            cacheInvokeContext.setMethod(method);
            cacheInvokeContext.setArgs(new Object[]{new String[]{"K1"}, 10});
            cacheInvokeContext.setInvoker(() -> method.invoke(new TestMulti(), cacheInvokeContext.getArgs()));

            cache.put("K1", 1);
            cache.put("K2", 2);
            updateAnnoConfig.setMulti(true);
            CacheHandler.invoke(cacheInvokeContext);
            assertEquals(1, cache.get("K1"));
            assertEquals(2, cache.get("K2"));
        }
    }
}
