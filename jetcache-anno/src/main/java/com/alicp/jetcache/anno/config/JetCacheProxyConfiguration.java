package com.alicp.jetcache.anno.config;

import com.alicp.jetcache.anno.aop.CacheAdvisor;
import com.alicp.jetcache.anno.aop.JetCacheInterceptor;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.context.annotation.Role;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created on 2016/11/16.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
@Configuration
public class JetCacheProxyConfiguration implements ImportAware {

    protected AnnotationAttributes enableJetCache;

    private ConcurrentHashMap configMap = new ConcurrentHashMap();

    @Autowired
    private GlobalCacheConfig globalCacheConfig;

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        this.enableJetCache = AnnotationAttributes.fromMap(
                importMetadata.getAnnotationAttributes(EnableMethodCache.class.getName(), false));
        if (this.enableJetCache == null) {
            throw new IllegalArgumentException(
                    "@EnableJetCache is not present on importing class " + importMetadata.getClassName());
        }
    }

    @Bean(name = CacheAdvisor.CACHE_ADVISOR_BEAN_NAME)
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public CacheAdvisor jetcacheAdvisor(JetCacheInterceptor jetCacheInterceptor) {
        CacheAdvisor advisor = new CacheAdvisor();
        advisor.setAdviceBeanName(CacheAdvisor.CACHE_ADVISOR_BEAN_NAME);
        advisor.setAdvice(jetCacheInterceptor);
        advisor.setBasePackages(this.enableJetCache.getStringArray("basePackages"));
        advisor.setCacheConfigMap(configMap);
        advisor.setOrder(this.enableJetCache.<Integer>getNumber("order"));
        return advisor;
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public JetCacheInterceptor jetcacheInterceptor() {
        JetCacheInterceptor interceptor = new JetCacheInterceptor();
        interceptor.setCacheConfigMap(configMap);
        interceptor.setGlobalCacheConfig(globalCacheConfig);
        return interceptor;
    }

}