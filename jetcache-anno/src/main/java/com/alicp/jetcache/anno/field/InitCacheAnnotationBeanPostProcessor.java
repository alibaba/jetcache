package com.alicp.jetcache.anno.field;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheConfigException;
import com.alicp.jetcache.anno.CacheConsts;
import com.alicp.jetcache.anno.CreateCache;
import com.alicp.jetcache.anno.method.ClassUtil;
import com.alicp.jetcache.anno.support.CacheAnnoConfig;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created on 2016/12/9.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor
 */
public class InitCacheAnnotationBeanPostProcessor extends AutowiredAnnotationBeanPostProcessor {

    private static Logger logger = LoggerFactory.getLogger(InitCacheAnnotationBeanPostProcessor.class);

    private ConfigurableListableBeanFactory beanFactory;

    private GlobalCacheConfig globalCacheConfig;
    private String globalCacheConfigBeanName;

    private final Map<String, InjectionMetadata> injectionMetadataCache = new ConcurrentHashMap<String, InjectionMetadata>();

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        super.setBeanFactory(beanFactory);
        if (!(beanFactory instanceof ConfigurableListableBeanFactory)) {
            throw new IllegalArgumentException(
                    "AutowiredAnnotationBeanPostProcessor requires a ConfigurableListableBeanFactory");
        }
        this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
    }

    @Override
    public PropertyValues postProcessPropertyValues(
            PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeanCreationException {
        Map<String, GlobalCacheConfig> beans = beanFactory.getBeansOfType(GlobalCacheConfig.class);
        if (beans == null || beans.size() == 0) {
            throw new CacheConfigException("requires a GlobalCacheConfig");
        } else if (beans.size() > 1) {
            throw new CacheConfigException("more than one GlobalCacheConfig");
        } else {
            globalCacheConfigBeanName = beans.keySet().iterator().next();
            globalCacheConfig = beans.get(globalCacheConfigBeanName);
        }

        InjectionMetadata metadata = findAutowiringMetadata(beanName, bean.getClass(), pvs);
        try {
            metadata.inject(bean, beanName, pvs);
        } catch (BeanCreationException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new BeanCreationException(beanName, "Injection of autowired dependencies failed", ex);
        }
        return pvs;
    }

    private InjectionMetadata findAutowiringMetadata(String beanName, Class<?> clazz, PropertyValues pvs) {
        // Fall back to class name as cache key, for backwards compatibility with custom callers.
        String cacheKey = (StringUtils.hasLength(beanName) ? beanName : clazz.getName());
        // Quick check on the concurrent map first, with minimal locking.
        InjectionMetadata metadata = this.injectionMetadataCache.get(cacheKey);
        if (InjectionMetadata.needsRefresh(metadata, clazz)) {
            synchronized (this.injectionMetadataCache) {
                metadata = this.injectionMetadataCache.get(cacheKey);
                if (InjectionMetadata.needsRefresh(metadata, clazz)) {
                    if (metadata != null) {
                        metadata.clear(pvs);
                    }
                    try {
                        metadata = buildAutowiringMetadata(clazz);
                        this.injectionMetadataCache.put(cacheKey, metadata);
                    } catch (NoClassDefFoundError err) {
                        throw new IllegalStateException("Failed to introspect bean class [" + clazz.getName() +
                                "] for autowiring metadata: could not find class that it depends on", err);
                    }
                }
            }
        }
        return metadata;
    }

    private InjectionMetadata buildAutowiringMetadata(final Class<?> clazz) {
        LinkedList<InjectionMetadata.InjectedElement> elements = new LinkedList<InjectionMetadata.InjectedElement>();
        Class<?> targetClass = clazz;

        do {
            final LinkedList<InjectionMetadata.InjectedElement> currElements =
                    new LinkedList<InjectionMetadata.InjectedElement>();

            ReflectionUtils.doWithLocalFields(targetClass, new ReflectionUtils.FieldCallback() {
                @Override
                public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                    CreateCache ann = field.getAnnotation(CreateCache.class);
                    if (ann != null) {
                        if (Modifier.isStatic(field.getModifiers())) {
                            if (logger.isWarnEnabled()) {
                                logger.warn("Autowired annotation is not supported on static fields: " + field);
                            }
                            return;
                        }
                        currElements.add(new AutowiredFieldElement(field, ann));
                    }
                }
            });

            elements.addAll(0, currElements);
            targetClass = targetClass.getSuperclass();
        }
        while (targetClass != null && targetClass != Object.class);

        return new InjectionMetadata(clazz, elements);
    }


    private class AutowiredFieldElement extends InjectionMetadata.InjectedElement {

        private Field field;
        private CreateCache ann;

        public AutowiredFieldElement(Field field, CreateCache ann) {
            super(field, null);
            this.field = field;
            this.ann = ann;
        }

        @Override
        protected void inject(Object bean, String beanName, PropertyValues pvs) throws Throwable {
            beanFactory.registerDependentBean(beanName, globalCacheConfigBeanName);

            CacheAnnoConfig cac = new CacheAnnoConfig();
            cac.setArea(ann.area());
            cac.setName(ann.name());
            cac.setExpire(ann.expire());
            cac.setCacheType(ann.cacheType());
            cac.setLocalLimit(ann.localLimit());
            cac.setVersion(ann.version());
            cac.setSerialPolicy(ann.serialPolicy());
            cac.setKeyConvertor(ann.keyConvertor());

            String cacheName = cac.getName();
            if (CacheConsts.DEFAULT_NAME.equalsIgnoreCase(cacheName)) {
                StringBuilder sb = new StringBuilder();
                sb.append(field.getDeclaringClass().getName());
                sb.append(".").append(field.getName());
                ClassUtil.removeHiddenPackage(globalCacheConfig.getHiddenPackages(), sb);
                cacheName = sb.toString();
            }
            if (cac.getVersion() != CacheConsts.DEFAULT_VERSION) {
                cacheName = cac.getVersion() + "_" + cacheName;
            }
            String fullCacheName = cac.getArea() + "_" + cacheName;
            Cache cache = globalCacheConfig.getCacheContext().__createOrGetCache(cac, ann.area(), fullCacheName);

            field.setAccessible(true);
            field.set(bean, cache);
        }
    }

}
