package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.CacheConfigException;
import com.alicp.jetcache.external.ExternalCacheWriteInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.List;

/**
 * Spring-based parser for external write interceptor bean references.
 */
public class DefaultSpringExternalWriteInterceptorParser
        implements ExternalWriteInterceptorParser, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public List<ExternalCacheWriteInterceptor> parseExternalWriteInterceptors(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        List<ExternalCacheWriteInterceptor> interceptors = new ArrayList<>();
        String[] beanRefs = value.split(",");
        for (String beanRef : beanRefs) {
            beanRef = beanRef.trim();
            if (!beanRef.isEmpty()) {
                String beanName = DefaultSpringEncoderParser.parseBeanName(beanRef);
                if (beanName == null) {
                    throw new CacheConfigException("externalWriteInterceptors should use bean: prefix: " + beanRef);
                }
                try {
                    ExternalCacheWriteInterceptor interceptor =
                            applicationContext.getBean(beanName, ExternalCacheWriteInterceptor.class);
                    interceptors.add(interceptor);
                } catch (Exception e) {
                    throw new CacheConfigException(
                            "ExternalCacheWriteInterceptor bean not found or invalid: " + beanName, e);
                }
            }
        }
        return interceptors.isEmpty() ? null : interceptors;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
