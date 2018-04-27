package com.alicp.jetcache.autoconfigure;

import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import com.alicp.jetcache.anno.support.SpringConfigProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Created on 2016/11/17.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
@Configuration
@ConditionalOnClass(GlobalCacheConfig.class)
@ConditionalOnMissingBean(GlobalCacheConfig.class)
@EnableConfigurationProperties(JetCacheProperties.class)
@Import({RedisAutoConfiguration.class,
        CaffeineAutoConfiguration.class,
        LinkedHashMapAutoConfiguration.class,
        RedisLettuceAutoConfiguration.class,
        RedisLettuce4AutoConfiguration.class})
public class JetCacheAutoConfiguration {

    public static final String GLOBAL_CACHE_CONFIG_NAME = "globalCacheConfig";

    private SpringConfigProvider _springConfigProvider = new SpringConfigProvider();

    private AutoConfigureBeans _autoConfigureBeans = new AutoConfigureBeans();

    private GlobalCacheConfig _globalCacheConfig;

    @Bean
    @ConditionalOnMissingBean
    public SpringConfigProvider springConfigProvider() {
        return _springConfigProvider;
    }

    @Bean
    public AutoConfigureBeans autoConfigureBeans() {
        return _autoConfigureBeans;
    }

    @Bean
    public static BeanDependencyManager beanDependencyManager(){
        return new BeanDependencyManager();
    }

    @Bean(name = GLOBAL_CACHE_CONFIG_NAME)
    public GlobalCacheConfig globalCacheConfig(SpringConfigProvider configProvider,
                                                            AutoConfigureBeans autoConfigureBeans,
                                                            JetCacheProperties props) {
        if (_globalCacheConfig != null) {
            return _globalCacheConfig;
        }
        _globalCacheConfig = new GlobalCacheConfig();
        _globalCacheConfig.setConfigProvider(configProvider);
        _globalCacheConfig.setHiddenPackages(props.getHidePackages());
        _globalCacheConfig.setStatIntervalMinutes(props.getStatIntervalMinutes());
        _globalCacheConfig.setAreaInCacheName(props.isAreaInCacheName());
        _globalCacheConfig.setPenetrationProtect(props.isPenetrationProtect());
        _globalCacheConfig.setLocalCacheBuilders(autoConfigureBeans.getLocalCacheBuilders());
        _globalCacheConfig.setRemoteCacheBuilders(autoConfigureBeans.getRemoteCacheBuilders());
        return _globalCacheConfig;
    }

}
