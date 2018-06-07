package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.anno.SerialPolicy;
import com.alicp.jetcache.anno.method.SpringCacheContext;
import com.alicp.jetcache.support.JavaValueDecoder;
import com.alicp.jetcache.support.SpringJavaValueDecoder;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.function.Function;

/**
 * Created on 2016/12/1.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class SpringConfigProvider extends ConfigProvider implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public CacheContext newContext(GlobalCacheConfig globalCacheConfig) {
        return new SpringCacheContext(globalCacheConfig, applicationContext);
    }

    private String parseBeanName(String str) {
        final String beanPrefix = "bean:";
        int len = beanPrefix.length();
        if (str != null && str.startsWith(beanPrefix) && str.length() > len) {
            return str.substring(len);
        } else {
            return null;
        }
    }

    @Override
    public Function<Object, byte[]> parseValueEncoder(String valueEncoder) {
        String beanName = parseBeanName(valueEncoder);
        if (beanName == null) {
            return super.parseValueEncoder(valueEncoder);
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
    public Function<byte[], Object> parseValueDecoder(String valueDecoder) {
        String beanName = parseBeanName(valueDecoder);
        if (beanName == null) {
            return super.parseValueDecoder(valueDecoder);
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
    public Function<Object, Object> parseKeyConvertor(String convertor) {
        String beanName = parseBeanName(convertor);
        if (beanName == null) {
            return super.parseKeyConvertor(convertor);
        } else {
            return (Function<Object, Object>) applicationContext.getBean(beanName);
        }
    }
}
