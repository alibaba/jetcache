/**
 * Created on  13-09-18 16:37
 */
package com.alicp.jetcache.anno.config;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class CacheNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("annotation-driven", new CacheAnnotationParser());
    }
}
