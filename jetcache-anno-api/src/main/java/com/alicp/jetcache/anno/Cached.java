/**
 * Created on  13-09-04
 */
package com.alicp.jetcache.anno;

import java.lang.annotation.*;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cached {
    String area() default CacheConsts.DEFAULT_AREA;
    String name() default CacheConsts.UNDEFINED_STRING;
    boolean enabled() default CacheConsts.DEFAULT_ENABLED;
    int expire() default CacheConsts.UNDEFINED_INT;
    CacheType cacheType() default CacheType.REMOTE;
    int localLimit() default CacheConsts.UNDEFINED_INT;
    String serialPolicy() default CacheConsts.UNDEFINED_STRING;

    String keyConvertor() default CacheConsts.UNDEFINED_STRING;

    boolean cacheNullValue() default CacheConsts.DEFAULT_CACHE_NULL_VALUE;

    /**
     * Expression attribute used for conditioning the method caching.
     * <p>Default is "", meaning the method is always cached.
     */
    String condition() default CacheConsts.UNDEFINED_STRING;

    /**
     * Expression attribute used to veto method caching.
     * <p>Unlike {@link #condition()}, this expression is evaluated after the method
     * has been called and can therefore refer to the {@code result}. Default is "",
     * meaning that caching is never vetoed.
     */
    String unless() default CacheConsts.UNDEFINED_STRING;
}
