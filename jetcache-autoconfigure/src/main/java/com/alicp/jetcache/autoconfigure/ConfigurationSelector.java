package com.alicp.jetcache.autoconfigure;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

/**
 * Created on 2016/11/25.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class ConfigurationSelector implements ImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[]{RedisAutoConfiguration.class.getName()};
    }
}
