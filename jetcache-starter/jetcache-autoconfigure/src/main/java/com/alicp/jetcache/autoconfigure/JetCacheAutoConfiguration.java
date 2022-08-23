package com.alicp.jetcache.autoconfigure;

import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.SimpleCacheManager;
import com.alicp.jetcache.anno.support.EncoderParser;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import com.alicp.jetcache.anno.support.JetCacheBaseBeans;
import com.alicp.jetcache.anno.support.KeyConvertorParser;
import com.alicp.jetcache.anno.support.SpringConfigProvider;
import com.alicp.jetcache.support.StatInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.function.Consumer;

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
        MockRemoteCacheAutoConfiguration.class,
        LinkedHashMapAutoConfiguration.class,
        RedisLettuceAutoConfiguration.class,
        RedisSpringDataAutoConfiguration.class,
        RedissonAutoConfiguration.class})
public class JetCacheAutoConfiguration {

    public static final String GLOBAL_CACHE_CONFIG_NAME = "globalCacheConfig";

    @Bean
    @ConditionalOnMissingBean
    public SpringConfigProvider springConfigProvider(
            @Autowired ApplicationContext applicationContext,
            @Autowired GlobalCacheConfig globalCacheConfig,
            @Autowired(required = false) EncoderParser encoderParser,
            @Autowired(required = false) KeyConvertorParser keyConvertorParser,
            @Autowired(required = false) Consumer<StatInfo> metricsCallback) {
        return new JetCacheBaseBeans().springConfigProvider(applicationContext, globalCacheConfig,
                encoderParser, keyConvertorParser, metricsCallback);
    }

    @Bean(name = "jcCacheManager")
    @ConditionalOnMissingBean
    public CacheManager cacheManager(@Autowired SpringConfigProvider springConfigProvider) {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCacheBuilderTemplate(springConfigProvider.getCacheBuilderTemplate());
        return cacheManager;
    }

    @Bean
    public AutoConfigureBeans autoConfigureBeans() {
        return new AutoConfigureBeans();
    }

    @Bean
    public static BeanDependencyManager beanDependencyManager() {
        return new BeanDependencyManager();
    }

    @Bean(name = GLOBAL_CACHE_CONFIG_NAME)
    public GlobalCacheConfig globalCacheConfig(AutoConfigureBeans autoConfigureBeans, JetCacheProperties props) {
        GlobalCacheConfig _globalCacheConfig = new GlobalCacheConfig();
        _globalCacheConfig = new GlobalCacheConfig();
        _globalCacheConfig.setHiddenPackages(props.getHiddenPackages());
        _globalCacheConfig.setStatIntervalMinutes(props.getStatIntervalMinutes());
        _globalCacheConfig.setAreaInCacheName(props.isAreaInCacheName());
        _globalCacheConfig.setPenetrationProtect(props.isPenetrationProtect());
        _globalCacheConfig.setEnableMethodCache(props.isEnableMethodCache());
        _globalCacheConfig.setLocalCacheBuilders(autoConfigureBeans.getLocalCacheBuilders());
        _globalCacheConfig.setRemoteCacheBuilders(autoConfigureBeans.getRemoteCacheBuilders());
        return _globalCacheConfig;
    }

}
