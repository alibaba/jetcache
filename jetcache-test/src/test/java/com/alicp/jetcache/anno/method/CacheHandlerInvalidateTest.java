/**
 * Created on 2018/5/12.
 */
package com.alicp.jetcache.anno.method;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheConsts;
import com.alicp.jetcache.anno.support.CacheInvalidateAnnoConfig;
import com.alicp.jetcache.anno.support.ConfigMap;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import com.alicp.jetcache.embedded.LinkedHashMapCacheBuilder;
import com.alicp.jetcache.support.FastjsonKeyConvertor;
import com.alicp.jetcache.testsupport.CountClass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class CacheHandlerInvalidateTest {
    private GlobalCacheConfig globalCacheConfig;
    private CacheInvokeConfig cacheInvokeConfig;
    private CountClass count;
    private Cache cache;
    private ConfigMap configMap;
    private CacheInvalidateAnnoConfig invalidateAnnoConfig;
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


        invalidateAnnoConfig = new CacheInvalidateAnnoConfig();
        invalidateAnnoConfig.setDefineMethod(method);
        invalidateAnnoConfig.setCondition(CacheConsts.UNDEFINED_STRING);
        cacheInvokeConfig.setInvalidateAnnoConfig(invalidateAnnoConfig);

        invalidateAnnoConfig.setKey("args[0]");
        cacheInvokeConfig.setCachedAnnoConfig(null);
        cacheInvokeContext.setMethod(method);
        cacheInvokeContext.setArgs(new Object[]{"KEY", 1000});
        cacheInvokeContext.setInvoker(() -> cacheInvokeContext.getMethod().invoke(count, cacheInvokeContext.getArgs()));
        cacheInvokeContext.setCacheFunction((a, b) -> cache);

    }

    @Test
    public void testInvalidate() throws Throwable {
        cache.put("KEY", "V");
        CacheHandler.invoke(cacheInvokeContext);
        assertNull(cache.get("KEY"));
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
}
