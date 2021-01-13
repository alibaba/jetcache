package com.alicp.jetcache.anno.field;

import com.alicp.jetcache.anno.CreateCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created on 2016/12/9.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor
 */
@SuppressWarnings("AliMissingOverrideAnnotation")
@Component // necessary when spring boot version <= 1.2.X
public class CreateCacheAnnotationBeanPostProcessor extends AutowiredAnnotationBeanPostProcessor {

    private static Logger logger = LoggerFactory.getLogger(CreateCacheAnnotationBeanPostProcessor.class);

    private ConfigurableListableBeanFactory beanFactory;

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
        return postProcessProperties(pvs, bean, beanName);
    }

    //@Override
    //since 5.1
    @SuppressWarnings("AliMissingOverrideAnnotation")
    public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) {
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
                        clear(metadata, pvs);
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

    private volatile boolean clearInited = false;
    private Method cleanMethod;

    /**
     * clear method not found in Spring 4.0.
     * @param obj
     * @param param
     */
    private void clear(InjectionMetadata obj, PropertyValues param) {
        if(!clearInited){
            try {
                cleanMethod = InjectionMetadata.class.getMethod("clear", PropertyValues.class);
            } catch (NoSuchMethodException e) {
            }
            clearInited = true;
        }
        if (cleanMethod != null) {
            try {
                cleanMethod.invoke(obj, param);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private InjectionMetadata buildAutowiringMetadata(final Class<?> clazz) {
        LinkedList<InjectionMetadata.InjectedElement> elements = new LinkedList<InjectionMetadata.InjectedElement>();
        Class<?> targetClass = clazz;

        do {
            final LinkedList<InjectionMetadata.InjectedElement> currElements =
                    new LinkedList<InjectionMetadata.InjectedElement>();

            doWithLocalFields(targetClass, new ReflectionUtils.FieldCallback() {
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

    private void doWithLocalFields(Class clazz, ReflectionUtils.FieldCallback fieldCallback) {
        Field fs[] = clazz.getDeclaredFields();
        for (Field field : fs) {
            try {
                fieldCallback.doWith(field);
            } catch (IllegalAccessException ex) {
                throw new IllegalStateException("Not allowed to access field '" + field.getName() + "': " + ex);
            }
        }
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
            beanFactory.registerDependentBean(beanName, "globalCacheConfig");
            LazyInitCache cache = new LazyInitCache(beanFactory, ann, field);
            field.setAccessible(true);
            field.set(bean, cache);
        }
    }

}
