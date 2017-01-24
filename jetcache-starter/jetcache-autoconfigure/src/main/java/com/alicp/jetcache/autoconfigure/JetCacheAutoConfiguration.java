package com.alicp.jetcache.autoconfigure;

import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import com.alicp.jetcache.anno.support.SpringConfigProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Created on 2016/11/17.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
@Configuration
@ConditionalOnClass(GlobalCacheConfig.class)
@ConditionalOnMissingBean(GlobalCacheConfig.class)
@EnableConfigurationProperties(JetCacheProperties.class)
@Import({RedisAutoConfiguration.class, CaffeineAutoConfiguration.class, LinkedHashMapAutoConfiguration.class})
public class JetCacheAutoConfiguration {

    public JetCacheAutoConfiguration(){
    }

    private SpringConfigProvider springConfigProvider = new SpringConfigProvider();

    private AutoConfigureBeans autoConfigureBeans = new AutoConfigureBeans();

    private GlobalCacheConfig globalCacheConfig;

    @Autowired
    private JetCacheProperties props;

    @Bean
    @ConditionalOnMissingBean
    public SpringConfigProvider springConfigProvider() {
        return springConfigProvider;
    }

    @Bean
    public AutoConfigureBeans autoConfigureBeans(){
        return autoConfigureBeans;
    }

    @Bean
    public synchronized GlobalCacheConfig globalCacheConfig() {
        if (globalCacheConfig != null) {
            return globalCacheConfig;
        }
        globalCacheConfig = new GlobalCacheConfig();
        globalCacheConfig.setConfigProvider(springConfigProvider);
        globalCacheConfig.setHiddenPackages(props.getHidePackages());
        globalCacheConfig.setStatIntervalMinutes(props.getStatIntervalMinutes());
        globalCacheConfig.setLocalCacheBuilders(autoConfigureBeans.getLocalCacheBuilders());
        globalCacheConfig.setRemoteCacheBuilders(autoConfigureBeans.getRemoteCacheBuilders());
        return globalCacheConfig;
    }

}
