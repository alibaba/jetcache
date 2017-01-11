package com.alicp.jetcache.autoconfigure;

import com.alicp.jetcache.CacheBuilder;
import com.alicp.jetcache.anno.CacheConsts;
import com.alicp.jetcache.embedded.EmbeddedCacheBuilder;
import org.springframework.boot.bind.RelaxedPropertyResolver;

/**
 * Created on 2016/12/2.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public abstract class EmbeddedCacheAutoInit extends AbstractCacheAutoInit {

    public EmbeddedCacheAutoInit(String typeName) {
        super(typeName);
    }

    @Override
    protected void parseGeneralConfig(CacheBuilder builder, RelaxedPropertyResolver resolver) {
        super.parseGeneralConfig(builder, resolver);
        EmbeddedCacheBuilder ecb = (EmbeddedCacheBuilder) builder;

        ecb.limit(Integer.parseInt(resolver.getProperty("limit", String.valueOf(CacheConsts.DEFAULT_LOCAL_LIMIT))));
    }
}
