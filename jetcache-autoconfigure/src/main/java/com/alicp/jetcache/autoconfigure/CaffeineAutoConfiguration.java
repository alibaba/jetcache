package com.alicp.jetcache.autoconfigure;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.embedded.CaffeineCacheBuilder;
import com.alicp.jetcache.embedded.LinkedHashMapCacheBuilder;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

/**
 * Created on 2016/12/2.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
@Component
@Conditional(CaffeineAutoConfiguration.CaffeineCondition.class)
public class CaffeineAutoConfiguration extends EmbeddedCacheAutoConfiguration {
    public CaffeineAutoConfiguration() {
        super("caffeine");
    }

    @Override
    protected Cache initCache(RelaxedPropertyResolver resolver, String cacheArea) {
        CaffeineCacheBuilder builder = CaffeineCacheBuilder.createCaffeineCacheBuilder();
        parseGeneralConfig(builder, resolver);
        return builder.buildCache();
    }

    public static class CaffeineCondition extends JetCacheConditon {
        public CaffeineCondition() {
            super("caffeine");
        }
    }
}
