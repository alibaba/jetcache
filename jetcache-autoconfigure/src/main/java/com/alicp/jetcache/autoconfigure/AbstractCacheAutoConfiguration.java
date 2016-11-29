package com.alicp.jetcache.autoconfigure;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.external.ExternalCacheBuilder;
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
    protected Map localCacheBuilders;

    @Autowired
    protected Map remoteCacheBuilders;

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
        RelaxedPropertyResolver resolver = new RelaxedPropertyResolver(
                applicationContext.getEnvironment(), "jetcache.");
        process(resolver, "local.", localCacheBuilders);
        process(resolver, "remote.", remoteCacheBuilders);
    }

    private void process(RelaxedPropertyResolver resolver, String prefix, Map cacheBuilders) {
        Map<String, Object> m = resolver.getSubProperties(prefix);
        Set<String> cacheAreaNames = m.keySet().stream().map((s) -> s.substring(0, s.indexOf('.'))).collect(Collectors.toSet());
        for (String cacheArea : cacheAreaNames) {
            if (!typeName.equals(m.get(cacheArea + ".type"))) {
                continue;
            }
            RelaxedPropertyResolver r = new RelaxedPropertyResolver(resolver, prefix + "." + cacheArea);
            logger.info("init cache area {} , type= {}", cacheArea, typeName);
            Cache c = initCache(r, cacheArea);
            cacheBuilders.put(cacheArea, c);
        }
    }

    protected void parseGeneralConfig(ExternalCacheBuilder builder, RelaxedPropertyResolver resolver) {

    }

    protected abstract Cache initCache(RelaxedPropertyResolver resolver, String cacheArea);
}
