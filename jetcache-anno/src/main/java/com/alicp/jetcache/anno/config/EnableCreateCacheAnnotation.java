package com.alicp.jetcache.anno.config;

/**
 * Created on 2016/12/13.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */

import com.alicp.jetcache.anno.field.CreateCacheAnnotationBeanPostProcessor;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({CommonConfiguration.class, CreateCacheAnnotationBeanPostProcessor.class})
public @interface EnableCreateCacheAnnotation {
}
