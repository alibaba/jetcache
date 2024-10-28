package com.alicp.jetcache.autoconfigure;

import com.alicp.jetcache.CacheBuilder;
import com.alicp.jetcache.embedded.ConcurrentPatriciaTrieCacheBuilder;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

/**
 * Created on 2016/12/2.
 *
 * @author huangli
 */
@Component
@Conditional(ConcurrentPatriciaTrieCacheAutoConfiguration.ConcurrentPatriciaTrieCacheCondition.class)
public class ConcurrentPatriciaTrieCacheAutoConfiguration extends EmbeddedCacheAutoInit {
    public ConcurrentPatriciaTrieCacheAutoConfiguration() {
        super("patriciatrie");
    }

    @Override
    protected CacheBuilder initCache(ConfigTree ct, String cacheAreaWithPrefix) {
        ConcurrentPatriciaTrieCacheBuilder builder = ConcurrentPatriciaTrieCacheBuilder.createConcurrentPatriciaTrieCacheBuilder();
        parseGeneralConfig(builder, ct);
        return builder;
    }

    public static class ConcurrentPatriciaTrieCacheCondition extends JetCacheCondition {
        public ConcurrentPatriciaTrieCacheCondition() {
            super("patriciatrie");
        }
    }
}
