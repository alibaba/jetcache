/**
 * Created on  13-09-04
 */
package com.alicp.jetcache.anno;

import com.alicp.jetcache.CacheConsts;

import java.lang.annotation.*;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cached {
    String area() default CacheConsts.DEFAULT_AREA;
    boolean enabled() default CacheConsts.DEFAULT_ENABLED;
    int expire() default CacheConsts.DEFAULT_EXPIRE;
    CacheType cacheType() default CacheType.REMOTE;
    int localLimit() default CacheConsts.DEFAULT_LOCAL_LIMIT;
    int version() default CacheConsts.DEFAULT_VERSION;
    boolean cacheNullValue() default CacheConsts.DEFAULT_CACHE_NULL_VALUE;
    SerialPolicy serialPolicy() default SerialPolicy.KRYO;

    /**
     * Expression attribute used for conditioning the method caching.
     * <p>Default is "", meaning the method is always cached.
     */
    String condition() default CacheConsts.DEFAULT_CONDITION;

    /**
     * Expression attribute used to veto method caching.
     * <p>Unlike {@link #condition()}, this expression is evaluated after the method
     * has been called and can therefore refer to the {@code result}. Default is "",
     * meaning that caching is never vetoed.
     */
    String unless() default CacheConsts.DEFAULT_UNLESS;
}
