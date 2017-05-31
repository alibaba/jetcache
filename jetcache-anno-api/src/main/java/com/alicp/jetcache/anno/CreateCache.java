package com.alicp.jetcache.anno;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/12/9.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CreateCache {
    String area() default CacheConsts.DEFAULT_AREA;
    String name() default CacheConsts.UNDEFINED_STRING;
    TimeUnit timeUnit() default TimeUnit.SECONDS;
    int expire() default CacheConsts.UNDEFINED_INT;
    CacheType cacheType() default CacheType.REMOTE;
    int localLimit() default CacheConsts.UNDEFINED_INT;
    String serialPolicy() default CacheConsts.UNDEFINED_STRING;

    String keyConvertor() default CacheConsts.UNDEFINED_STRING;
}
