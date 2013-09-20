/**
 * Created on  13-09-18 18:18
 */
package com.taobao.geek.jetcache.spring;

import com.alibaba.fastjson.util.IdentityHashMap;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * @author yeli.hl
 */
public class JetCacheAnnotationParser implements BeanDefinitionParser {

    private static final String CACHE_ADVISOR_BEAN_NAME = JetCacheAnnotationParser.class.getPackage().getName() + ".internalCacheAdvisor";

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        doParse(element, parserContext);
        return null;
    }

    private synchronized void doParse(Element element, ParserContext parserContext) {
        if (!parserContext.getRegistry().containsBeanDefinition(CACHE_ADVISOR_BEAN_NAME)) {
            Object eleSource = parserContext.extractSource(element);

            RootBeanDefinition configMapDef = new RootBeanDefinition(IdentityHashMap.class);
            configMapDef.setSource(eleSource);
            configMapDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
            String configMapName = parserContext.getReaderContext().registerWithGeneratedName(configMapDef);

            RootBeanDefinition interceptorDef = new RootBeanDefinition(JetCacheInterceptor.class);
            interceptorDef.setSource(eleSource);
            interceptorDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
//            parseCacheManagerProperty(element, interceptorDef);
//            CacheNamespaceHandler.parseKeyGenerator(element, interceptorDef);
            interceptorDef.getPropertyValues().add("cacheConfigMap", new RuntimeBeanReference(configMapName));
            String interceptorName = parserContext.getReaderContext().registerWithGeneratedName(interceptorDef);

            RootBeanDefinition advisorDef = new RootBeanDefinition(JetCacheAdvisor.class);
            advisorDef.setSource(eleSource);
            advisorDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
            advisorDef.getPropertyValues().add("adviceBeanName", interceptorName);
            advisorDef.getPropertyValues().add("cacheConfigMap", new RuntimeBeanReference(configMapName));
            parserContext.getRegistry().registerBeanDefinition(CACHE_ADVISOR_BEAN_NAME, advisorDef);

            CompositeComponentDefinition compositeDef = new CompositeComponentDefinition(element.getTagName(),
                    eleSource);
            compositeDef.addNestedComponent(new BeanComponentDefinition(configMapDef, configMapName));
            compositeDef.addNestedComponent(new BeanComponentDefinition(interceptorDef, interceptorName));
            compositeDef.addNestedComponent(new BeanComponentDefinition(advisorDef, CACHE_ADVISOR_BEAN_NAME));
            parserContext.registerComponent(compositeDef);
        }
    }
}
