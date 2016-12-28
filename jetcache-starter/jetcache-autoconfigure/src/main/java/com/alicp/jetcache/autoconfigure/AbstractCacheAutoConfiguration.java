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
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public abstract class AbstractCacheAutoConfiguration implements ApplicationContextAware {

    private static Logger logger = LoggerFactory.getLogger(AbstractCacheAutoConfiguration.class);

    protected ApplicationContext applicationContext;

    @Autowired
    protected AutoConfigureBeans autoConfigureBeans;

    @Autowired
    protected ConfigProvider configProvider;

    protected String typeName;

    public AbstractCacheAutoConfiguration(String typeName) {
        this.typeName = typeName;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() {
        process("jetcache.local.", autoConfigureBeans.getLocalCacheBuilders(), true);
        process("jetcache.remote.", autoConfigureBeans.getRemoteCacheBuilders(), false);
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
        String expire = resolver.getProperty("defaultExpireInMillis");
        if (expire != null && !"".equalsIgnoreCase(expire.trim())) {
            acb.setDefaultExpireInMillis(Long.parseLong(expire));
        }
        String expireAfterAccess = resolver.getProperty("defaultExpireInMillis", "false");
        acb.setExpireAfterAccess(Boolean.parseBoolean(expireAfterAccess));
    }

    protected abstract CacheBuilder initCache(RelaxedPropertyResolver resolver, String cacheAreaWithPrefix);
}
