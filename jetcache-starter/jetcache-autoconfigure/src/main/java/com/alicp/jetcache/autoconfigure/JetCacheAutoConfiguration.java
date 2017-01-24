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

    public JetCacheAutoConfiguration() {
    }

    private SpringConfigProvider _springConfigProvider = new SpringConfigProvider();

    private AutoConfigureBeans _autoConfigureBeans = new AutoConfigureBeans();

    private GlobalCacheConfig _globalCacheConfig;

    @Autowired
    private JetCacheProperties props;

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
    public synchronized GlobalCacheConfig globalCacheConfig(SpringConfigProvider configProvider, AutoConfigureBeans autoConfigureBeans) {
        if (_globalCacheConfig != null) {
            return _globalCacheConfig;
        }
        _globalCacheConfig = new GlobalCacheConfig();
        _globalCacheConfig.setConfigProvider(configProvider);
        _globalCacheConfig.setHiddenPackages(props.getHidePackages());
        _globalCacheConfig.setStatIntervalMinutes(props.getStatIntervalMinutes());
        _globalCacheConfig.setLocalCacheBuilders(autoConfigureBeans.getLocalCacheBuilders());
        _globalCacheConfig.setRemoteCacheBuilders(autoConfigureBeans.getRemoteCacheBuilders());
        return _globalCacheConfig;
    }

}
