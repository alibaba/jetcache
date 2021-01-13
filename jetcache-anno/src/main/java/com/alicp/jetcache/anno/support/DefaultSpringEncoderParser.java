/**
 * Created on 2019/6/7.
 */
package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.CacheConfigException;
import com.alicp.jetcache.anno.SerialPolicy;
import com.alicp.jetcache.support.*;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class DefaultSpringEncoderParser extends DefaultEncoderParser implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    static String parseBeanName(String str) {
        final String beanPrefix = "bean:";
        int len = beanPrefix.length();
        if (str != null && str.startsWith(beanPrefix) && str.length() > len) {
            return str.substring(len);
        } else {
            return null;
        }
    }

    @Override
    public Function<Object, byte[]> parseEncoder(String valueEncoder) {
        String beanName = parseBeanName(valueEncoder);
        if (beanName == null) {
            return super.parseEncoder(valueEncoder);
        } else {
            Object bean = applicationContext.getBean(beanName);
            if (bean instanceof Function) {
                return (Function<Object, byte[]>) bean;
            } else {
                return ((SerialPolicy) bean).encoder();
            }
        }
    }

    @Override
    public Function<byte[], Object> parseDecoder(String valueDecoder) {
        String beanName = parseBeanName(valueDecoder);
        if (beanName == null) {
            return super.parseDecoder(valueDecoder);
        } else {
            Object bean = applicationContext.getBean(beanName);
            if (bean instanceof Function) {
                return (Function<byte[], Object>) bean;
            } else {
                return ((SerialPolicy)bean).decoder();
            }
        }
    }

    @Override
    JavaValueDecoder javaValueDecoder(boolean useIdentityNumber) {
        return new SpringJavaValueDecoder(useIdentityNumber);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
