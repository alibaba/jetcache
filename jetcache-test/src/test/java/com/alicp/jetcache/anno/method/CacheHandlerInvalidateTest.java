/**
 * Created on 2018/5/12.
 */
package com.alicp.jetcache.anno.method;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheConsts;
import com.alicp.jetcache.anno.support.CacheInvalidateAnnoConfig;
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
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class CacheHandlerInvalidateTest {
    private ConfigProvider configProvider;
    private CacheInvokeConfig cacheInvokeConfig;
    private CountClass count;
    private Cache cache;
    private ConfigMap configMap;
    private CacheInvalidateAnnoConfig invalidateAnnoConfig;
    private CacheInvokeContext cacheInvokeContext;
    private Method method;

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

        method = CountClass.class.getMethod("update", String.class, int.class);
        cacheInvokeContext = configProvider.getCacheContext().createCacheInvokeContext(configMap);
        cacheInvokeContext.setCacheInvokeConfig(cacheInvokeConfig);


        invalidateAnnoConfig = new CacheInvalidateAnnoConfig();
        invalidateAnnoConfig.setDefineMethod(method);
        invalidateAnnoConfig.setCondition(CacheConsts.UNDEFINED_STRING);
        cacheInvokeConfig.setInvalidateAnnoConfigs(Collections.singletonList(invalidateAnnoConfig));

        invalidateAnnoConfig.setKey("args[0]");
        cacheInvokeConfig.setCachedAnnoConfig(null);
        cacheInvokeContext.setMethod(method);
        cacheInvokeContext.setArgs(new Object[]{"KEY", 1000});
        cacheInvokeContext.setInvoker(() -> method.invoke(count, cacheInvokeContext.getArgs()));
        cacheInvokeContext.setCacheFunction((a, b) -> cache);

    }

    @AfterEach
    public void tearDown() {
        configProvider.shutdown();
    }

    @Test
    public void testInvalidate() throws Throwable {
        cache.put("KEY", "V");
        CacheHandler.invoke(cacheInvokeContext);
        assertNull(cache.get("KEY"));
    }

    @Test
    public void testInvalidates() throws Throwable {
        CacheInvalidateAnnoConfig anno1 = new CacheInvalidateAnnoConfig();
        anno1.setDefineMethod(method);
        anno1.setCondition(CacheConsts.UNDEFINED_STRING);
        anno1.setKey("args[0] + 1");
        CacheInvalidateAnnoConfig anno2 = new CacheInvalidateAnnoConfig();
        anno2.setDefineMethod(method);
        anno2.setCondition(CacheConsts.UNDEFINED_STRING);
        anno2.setKey("args[0] + 2");
        cacheInvokeConfig.setInvalidateAnnoConfigs(Arrays.asList(anno1, anno2));
        cache.put("KEY1", "V");
        cache.put("KEY2", "V");
        CacheHandler.invoke(cacheInvokeContext);
        assertNull(cache.get("KEY1"));
        assertNull(cache.get("KEY2"));
    }

    @Test
    public void testConditionTrue() throws Throwable {
        cache.put("KEY", "V");
        invalidateAnnoConfig.setCondition("args[1]==1000");
        CacheHandler.invoke(cacheInvokeContext);
        assertNull(cache.get("KEY"));
    }

    @Test
    public void testConditionFalse() throws Throwable {
        cache.put("KEY", "V");
        invalidateAnnoConfig.setCondition("args[1]!=1000");
        CacheHandler.invoke(cacheInvokeContext);
        assertNotNull(cache.get("KEY"));
    }

    @Test
    public void testBadCondition() throws Throwable {
        cache.put("KEY", "V");
        invalidateAnnoConfig.setCondition("bad condition");
        CacheHandler.invoke(cacheInvokeContext);
        assertNotNull(cache.get("KEY"));
    }

    @Test
    public void testBadKey() throws Throwable {
        cache.put("KEY", "V");
        invalidateAnnoConfig.setKey("bad key script");
        CacheHandler.invoke(cacheInvokeContext);
        assertNotNull(cache.get("KEY"));
    }


    static class TestMulti {
        public void update(String keys) {
        }

        public void update(String keys[]) {
        }
    }

    @Test
    public void testMulti() throws Throwable {
        {
            Method method = TestMulti.class.getMethod("update", String[].class);
            invalidateAnnoConfig.setDefineMethod(method);
            invalidateAnnoConfig.setKey("args[0]");
            cacheInvokeContext.setMethod(method);
            cacheInvokeContext.setArgs(new Object[]{new String[]{"K1", "K2"}});
            cacheInvokeContext.setInvoker(() -> method.invoke(new TestMulti(), cacheInvokeContext.getArgs()));

            cache.put("K1", "V1");
            cache.put("K2", "V1");

            CacheHandler.invoke(cacheInvokeContext);
            assertNotNull(cache.get("K1"));
            assertNotNull(cache.get("K2"));

            invalidateAnnoConfig.setMulti(true);

            cacheInvokeContext.setArgs(new Object[]{null});
            CacheHandler.invoke(cacheInvokeContext);
            assertNotNull(cache.get("K1"));
            assertNotNull(cache.get("K2"));

            cacheInvokeContext.setArgs(new Object[]{new String[]{"K1", "K2"}});
            CacheHandler.invoke(cacheInvokeContext);
            assertNull(cache.get("K1"));
            assertNull(cache.get("K2"));
        }
        {
            cache.put("K1", "V1");
            Method method = TestMulti.class.getMethod("update", String.class);
            invalidateAnnoConfig.setDefineMethod(method);
            invalidateAnnoConfig.setKey("args[0]");
            cacheInvokeContext.setMethod(method);
            cacheInvokeContext.setArgs(new Object[]{"K1"});
            cacheInvokeContext.setInvoker(() -> method.invoke(new TestMulti(), cacheInvokeContext.getArgs()));

            invalidateAnnoConfig.setMulti(true);
            CacheHandler.invoke(cacheInvokeContext);
            assertNotNull(cache.get("K1"));
        }
    }
}
