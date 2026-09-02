package com.alicp.jetcache.autoconfigure;

import com.alicp.jetcache.CacheBuilder;
import com.alicp.jetcache.CacheConfigException;
import com.alicp.jetcache.anno.CacheConsts;
import com.alicp.jetcache.anno.support.ParserFunction;
import com.alicp.jetcache.external.ExternalCacheBuilder;
import com.alicp.jetcache.external.ExternalCacheWriteInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.Map;

/**
 * Created on 2016/11/29.
 *
 * @author huangli
 */
public abstract class ExternalCacheAutoInit extends AbstractCacheAutoInit {
    @Autowired(required = false)
    protected ApplicationContext applicationContext;

    public ExternalCacheAutoInit(String... cacheTypes) {
        super(cacheTypes);
    }

    @Override
    protected void parseGeneralConfig(CacheBuilder builder, ConfigTree ct) {
        super.parseGeneralConfig(builder, ct);
        ExternalCacheBuilder ecb = (ExternalCacheBuilder) builder;
        ecb.setKeyPrefix(ct.getProperty("keyPrefix"));
        ecb.setBroadcastChannel(parseBroadcastChannel(ct));
        ecb.setValueEncoder(new ParserFunction(ct.getProperty("valueEncoder", CacheConsts.DEFAULT_SERIAL_POLICY)));
        ecb.setValueDecoder(new ParserFunction(ct.getProperty("valueDecoder", CacheConsts.DEFAULT_SERIAL_POLICY)));
        parseWriteInterceptors(ecb, ct);
    }

    protected void parseWriteInterceptors(ExternalCacheBuilder ecb, ConfigTree ct) {
        String interceptorBeans = ct.getProperty("externalWriteInterceptors");
        if (interceptorBeans == null || interceptorBeans.trim().isEmpty()) {
            return;
        }
        String[] beanRefs = interceptorBeans.split(",");
        for (String beanRef : beanRefs) {
            beanRef = beanRef.trim();
            if (!beanRef.isEmpty()) {
                Object interceptor;
                try {
                    interceptor = resolveBean(beanRef);
                } catch (Exception e) {
                    throw new CacheConfigException("externalWriteInterceptors bean resolve failed: " + beanRef, e);
                }
                if (interceptor == null) {
                    throw new CacheConfigException("externalWriteInterceptors bean not found: " + beanRef);
                }
                if (!(interceptor instanceof ExternalCacheWriteInterceptor)) {
                    throw new CacheConfigException("externalWriteInterceptors bean is not ExternalCacheWriteInterceptor: " + beanRef);
                }
                ecb.addWriteInterceptor((ExternalCacheWriteInterceptor) interceptor);
            }
        }
    }

    private Object resolveBean(String beanRef) {
        if (autoConfigureBeans == null) {
            return null;
        }
        Map<String, Object> customContainer = autoConfigureBeans.getCustomContainer();

        String beanPrefix = "bean:";
        if (beanRef.startsWith(beanPrefix)) {
            String beanName = beanRef.substring(beanPrefix.length());
            if (applicationContext != null) {
                return applicationContext.getBean(beanName);
            }
            return customContainer.get(beanName);
        }

        return customContainer.get(beanRef);
    }

    protected String parseBroadcastChannel(ConfigTree ct) {
        String broadcastChannel = ct.getProperty("broadcastChannel");
        if (broadcastChannel != null && !"".equals(broadcastChannel.trim())) {
            return broadcastChannel.trim();
        } else {
            return null;
        }
    }
}
