package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.anno.method.SpringCacheContext;
import com.alicp.jetcache.support.StatInfo;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

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
        externalWriteInterceptorParser = new DefaultSpringExternalWriteInterceptorParser();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        if (encoderParser instanceof ApplicationContextAware) {
            ((ApplicationContextAware) encoderParser).setApplicationContext(applicationContext);
        }
        if (keyConvertorParser instanceof ApplicationContextAware) {
            ((ApplicationContextAware) keyConvertorParser).setApplicationContext(applicationContext);
        }
        if (externalWriteInterceptorParser instanceof ApplicationContextAware) {
            ((ApplicationContextAware) externalWriteInterceptorParser).setApplicationContext(applicationContext);
        }
    }

    @Override
    protected void doInit() {
        if (encoderParser instanceof ApplicationContextAware) {
            ((ApplicationContextAware) encoderParser).setApplicationContext(applicationContext);
        }
        if (keyConvertorParser instanceof ApplicationContextAware) {
            ((ApplicationContextAware) keyConvertorParser).setApplicationContext(applicationContext);
        }
        if (externalWriteInterceptorParser instanceof ApplicationContextAware) {
            ((ApplicationContextAware) externalWriteInterceptorParser).setApplicationContext(applicationContext);
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
    public void setExternalWriteInterceptorParser(ExternalWriteInterceptorParser externalWriteInterceptorParser) {
        super.setExternalWriteInterceptorParser(externalWriteInterceptorParser);
    }

    @Autowired(required = false)
    @Override
    public void setMetricsCallback(Consumer<StatInfo> metricsCallback) {
        super.setMetricsCallback(metricsCallback);
    }
}
