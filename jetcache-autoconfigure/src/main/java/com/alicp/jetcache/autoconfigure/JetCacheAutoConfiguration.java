package com.alicp.jetcache.autoconfigure;

import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import com.alicp.jetcache.anno.support.SpringGlobalCacheConfig;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 2016/11/17.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
@Configuration
@ConditionalOnClass(GlobalCacheConfig.class)
@ConditionalOnMissingBean(GlobalCacheConfig.class)
@EnableConfigurationProperties(JetCacheProperties.class)
@Import({JetCacheAutoConfiguration.Builders.class, RedisConfiguration.class})
public class JetCacheAutoConfiguration implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Autowired
    private JetCacheProperties props;

    private Map localCacheBuilders = new HashMap<>();

    private Map remoteCacheBuilders = new HashMap<>();;

    @Bean
    public SpringGlobalCacheConfig globalCacheConfig() {
        SpringGlobalCacheConfig c = new SpringGlobalCacheConfig();
        c.setApplicationContext(applicationContext);
        c.setHidePackages(props.getHidePackages());
        c.setStatIntervalMinutes(props.getStatIntervalMinutes());
        c.setStatCallback(props.getStatCallback());
        c.setLocalCacheBuilders(localCacheBuilders);
        c.setRemoteCacheBuilders(remoteCacheBuilders);
        return c;
    }

    @Bean
    public Map localCacheBuilders() {
        return localCacheBuilders;
    }

    @Bean
    public Map remoteCacheBuilders() {
        return remoteCacheBuilders;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Configuration
    public static class Builders {
//        @Bean
//        public Map localCacheBuilders() {
//            return new HashMap<>();
//        }
//
//        @Bean
//        public Map remoteCacheBuilders() {
//            return new HashMap<>();
//        }

    }


}
