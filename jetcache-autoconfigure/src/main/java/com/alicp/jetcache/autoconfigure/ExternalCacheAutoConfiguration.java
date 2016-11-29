package com.alicp.jetcache.autoconfigure;

import com.alicp.jetcache.external.ExternalCacheBuilder;
import org.springframework.boot.bind.RelaxedPropertyResolver;

/**
 * Created on 2016/11/29.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public abstract class ExternalCacheAutoConfiguration extends AbstractCacheAutoConfiguration {
    public ExternalCacheAutoConfiguration(String typeName) {
        super(typeName);
    }

    @Override
    protected void parseGeneralConfig(ExternalCacheBuilder builder, RelaxedPropertyResolver resolver) {
        super.parseGeneralConfig(builder, resolver);

    }
}
