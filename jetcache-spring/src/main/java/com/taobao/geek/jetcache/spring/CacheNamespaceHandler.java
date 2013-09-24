/**
 * Created on  13-09-18 16:37
 */
package com.taobao.geek.jetcache.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author yeli.hl
 */
public class CacheNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("annotation-driven", new CacheAnnotationParser());
    }
}
