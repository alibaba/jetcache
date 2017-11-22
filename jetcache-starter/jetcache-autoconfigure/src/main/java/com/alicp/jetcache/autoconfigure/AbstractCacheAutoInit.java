package com.alicp.jetcache.autoconfigure;

import com.alicp.jetcache.AbstractCacheBuilder;
import com.alicp.jetcache.CacheBuilder;
import com.alicp.jetcache.anno.support.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created on 2016/11/29.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public abstract class AbstractCacheAutoInit {

    private static Logger logger = LoggerFactory.getLogger(AbstractCacheAutoInit.class);

    @Autowired
    protected ConfigurableEnvironment environment;

    @Autowired
    protected AutoConfigureBeans autoConfigureBeans;

    @Autowired
    protected ConfigProvider configProvider;

    protected String typeName;

    private boolean inited = false;

    public AbstractCacheAutoInit(String cacheType) {
        this.typeName = cacheType;
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
        ConfigTree resolver = new ConfigTree(environment, prefix);
        Map<String, Object> m = resolver.getProperties();
        Set<String> cacheAreaNames = m.keySet().stream().map((s) -> s.substring(0, s.indexOf('.'))).collect(Collectors.toSet());
        for (String cacheArea : cacheAreaNames) {
            if (!typeName.equals(m.get(cacheArea + ".type"))) {
                continue;
            }
            ConfigTree ct = resolver.subTree(cacheArea + ".");
            logger.info("init cache area {} , type= {}", cacheArea, typeName);
            CacheBuilder c = initCache(ct, local ? "local." + cacheArea : "remote." + cacheArea);
            cacheBuilders.put(cacheArea, c);
        }
    }

    protected void parseGeneralConfig(CacheBuilder builder, ConfigTree ct) {
        AbstractCacheBuilder acb = (AbstractCacheBuilder) builder;
        acb.keyConvertor(configProvider.parseKeyConvertor(ct.getProperty("keyConvertor")));

        String expireAfterWriteInMillis = ct.getProperty("expireAfterWriteInMillis");
        if (expireAfterWriteInMillis == null) {
            // compatible with 2.1
            expireAfterWriteInMillis = ct.getProperty("defaultExpireInMillis");
        }
        if (expireAfterWriteInMillis != null) {
            acb.setExpireAfterWriteInMillis(Long.parseLong(expireAfterWriteInMillis));
        }

        String expireAfterAccessInMillis = ct.getProperty("expireAfterAccessInMillis");
        if (expireAfterAccessInMillis != null) {
            acb.setExpireAfterAccessInMillis(Long.parseLong(expireAfterAccessInMillis));
        }

    }

    protected abstract CacheBuilder initCache(ConfigTree ct, String cacheAreaWithPrefix);
}
