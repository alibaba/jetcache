package com.alicp.jetcache.anno.starter;

import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created on 2016/11/17.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
@Configuration
@ConditionalOnClass(GlobalCacheConfig.class)
@ConditionalOnMissingBean(GlobalCacheConfig.class)
public class JetCacheAutoConfiguration {

    @Bean
    public GlobalCacheConfig config(){
        return new GlobalCacheConfig();
    }
}
