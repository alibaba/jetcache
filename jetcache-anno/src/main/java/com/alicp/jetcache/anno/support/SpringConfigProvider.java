package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.anno.method.SpringCacheContext;
import com.alicp.jetcache.external.ExternalCacheWriteInterceptor;
import com.alicp.jetcache.support.StatInfo;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created on 2016/12/1.
 *
 * @author huangli
 */
public class SpringConfigProvider extends ConfigProvider implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    public SpringConfigProvider() {
        super();
        encoderParser = new DefaultSpringEncoderParser();
        keyConvertorParser = new DefaultSpringKeyConvertorParser();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    protected void doInit() {
        if (encoderParser instanceof ApplicationContextAware) {
            ((ApplicationContextAware) encoderParser).setApplicationContext(applicationContext);
        }
        if (keyConvertorParser instanceof ApplicationContextAware) {
            ((ApplicationContextAware) keyConvertorParser).setApplicationContext(applicationContext);
        }
        super.doInit();
    }

    @Override
    public CacheContext newContext(CacheManager cacheManager) {
        return new SpringCacheContext(cacheManager, this, globalCacheConfig, applicationContext);
    }

    @Autowired(required = false)
    @Override
    public void setEncoderParser(EncoderParser encoderParser) {
        super.setEncoderParser(encoderParser);
    }

    @Autowired(required = false)
    @Override
    public void setKeyConvertorParser(KeyConvertorParser keyConvertorParser) {
        super.setKeyConvertorParser(keyConvertorParser);
    }

    @Autowired(required = false)
    @Override
    public void setMetricsCallback(Consumer<StatInfo> metricsCallback) {
        super.setMetricsCallback(metricsCallback);
    }

    /**
     * Parse external write interceptor bean names from string.
     *
     * @param writeInterceptorNames comma-separated bean names
     * @return list of external write interceptors
     */
    public List<ExternalCacheWriteInterceptor> parseWriteInterceptors(String writeInterceptorNames) {
        if (writeInterceptorNames == null || writeInterceptorNames.trim().isEmpty()) {
            return null;
        }
        List<ExternalCacheWriteInterceptor> interceptors = new ArrayList<>();
        String[] beanNames = writeInterceptorNames.split(",");
        for (String beanName : beanNames) {
            beanName = beanName.trim();
            if (!beanName.isEmpty()) {
                try {
                    ExternalCacheWriteInterceptor interceptor = applicationContext.getBean(beanName, ExternalCacheWriteInterceptor.class);
                    interceptors.add(interceptor);
                } catch (Exception e) {
                    throw new com.alicp.jetcache.CacheConfigException(
                            "ExternalCacheWriteInterceptor bean not found or invalid: " + beanName, e);
                }
            }
        }
        return interceptors.isEmpty() ? null : interceptors;
    }

}
