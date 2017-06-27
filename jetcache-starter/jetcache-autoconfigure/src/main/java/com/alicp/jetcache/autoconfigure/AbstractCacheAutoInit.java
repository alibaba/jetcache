package com.alicp.jetcache.autoconfigure;

import com.alicp.jetcache.AbstractCacheBuilder;
import com.alicp.jetcache.CacheBuilder;
import com.alicp.jetcache.anno.support.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created on 2016/11/29.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public abstract class AbstractCacheAutoInit implements ApplicationContextAware {

    private static Logger logger = LoggerFactory.getLogger(AbstractCacheAutoInit.class);

    protected ApplicationContext applicationContext;

    @Autowired
    protected AutoConfigureBeans autoConfigureBeans;

    @Autowired
    protected ConfigProvider configProvider;

    protected String typeName;

    private boolean inited = false;

    public AbstractCacheAutoInit(String cacheType) {
        this.typeName = cacheType;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() {
        if (!inited) {
            synchronized (this) {
                if (!inited) {
                    process("jetcache.local.", autoConfigureBeans.getLocalCacheBuilders(), true);
                    process("jetcache.remote.", autoConfigureBeans.getRemoteCacheBuilders(), false);
                    inited = true;
                }
            }
        }
    }

    private void process(String prefix, Map cacheBuilders, boolean local) {
        RelaxedPropertyResolver resolver = new RelaxedPropertyResolver(
                applicationContext.getEnvironment(), prefix);
        Map<String, Object> m = resolver.getSubProperties("");
        Set<String> cacheAreaNames = m.keySet().stream().map((s) -> s.substring(0, s.indexOf('.'))).collect(Collectors.toSet());
        for (String cacheArea : cacheAreaNames) {
            if (!typeName.equals(m.get(cacheArea + ".type"))) {
                continue;
            }
            RelaxedPropertyResolver r = new RelaxedPropertyResolver(applicationContext.getEnvironment(), prefix + cacheArea + ".");
            logger.info("init cache area {} , type= {}", cacheArea, typeName);
            CacheBuilder c = initCache(r, local ? "local." + cacheArea : "remote." + cacheArea);
            cacheBuilders.put(cacheArea, c);
        }
    }

    protected void parseGeneralConfig(CacheBuilder builder, RelaxedPropertyResolver resolver) {
        AbstractCacheBuilder acb = (AbstractCacheBuilder) builder;
        acb.keyConvertor(configProvider.parseKeyConvertor(resolver.getProperty("keyConvertor")));

        String expireAfterWriteInMillis = resolver.getProperty("expireAfterWriteInMillis");
        if (expireAfterWriteInMillis == null) {
            // compatible with 2.1
            expireAfterWriteInMillis = resolver.getProperty("defaultExpireInMillis");
        }
        if (expireAfterWriteInMillis != null) {
            acb.setExpireAfterWriteInMillis(Long.parseLong(expireAfterWriteInMillis));
        }

        String expireAfterAccessInMillis = resolver.getProperty("expireAfterAccessInMillis");
        if (expireAfterAccessInMillis != null) {
            acb.setExpireAfterAccessInMillis(Long.parseLong(expireAfterAccessInMillis));
        }

    }

    protected abstract CacheBuilder initCache(RelaxedPropertyResolver resolver, String cacheAreaWithPrefix);
}
