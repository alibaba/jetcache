/**
 * Created on 2018/5/11.
 */
package com.alicp.jetcache.anno.method;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheConsts;
import com.alicp.jetcache.anno.support.CacheUpdateAnnoConfig;
import com.alicp.jetcache.anno.support.ConfigMap;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import com.alicp.jetcache.embedded.LinkedHashMapCacheBuilder;
import com.alicp.jetcache.support.FastjsonKeyConvertor;
import com.alicp.jetcache.testsupport.CountClass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class CacheHandlerUpdateTest {
    private GlobalCacheConfig globalCacheConfig;
    private CacheInvokeConfig cacheInvokeConfig;
    private CountClass count;
    private Cache cache;
    private ConfigMap configMap;
    private CacheUpdateAnnoConfig updateAnnoConfig;
    private CacheInvokeContext cacheInvokeContext;

    @BeforeEach
    public void setup() throws Exception {
        globalCacheConfig = new GlobalCacheConfig();
        globalCacheConfig.setLocalCacheBuilders(new HashMap<>());
        globalCacheConfig.setRemoteCacheBuilders(new HashMap<>());
        globalCacheConfig.init();
        cache = LinkedHashMapCacheBuilder.createLinkedHashMapCacheBuilder()
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .buildCache();


        cacheInvokeConfig = new CacheInvokeConfig();

        configMap = new ConfigMap();

        count = new CountClass();

        Method method = CountClass.class.getMethod("update", String.class, int.class);
        cacheInvokeContext = globalCacheConfig.getCacheContext().createCacheInvokeContext(configMap);
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
}
