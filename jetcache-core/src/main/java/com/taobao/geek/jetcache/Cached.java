/**
 * Created on  13-09-04
 */
package com.taobao.geek.jetcache;

import java.lang.annotation.*;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cached {
    public abstract String area() default CacheConfig.DEFAULT_AREA;
    public abstract boolean enabled() default CacheConfig.DEFAULT_ENABLED;
    public abstract int expire() default CacheConfig.DEFAULT_EXPIRE;
    public abstract CacheType cacheType() default CacheType.REMOTE;
    public abstract int localLimit() default CacheConfig.DEFAULT_LOCAL_LIMIT;
    public abstract int version() default CacheConfig.DEFAULT_VERSION;
    public abstract boolean cacheNullValue() default CacheConfig.DEFAULT_CACHE_NULL_VALUE;
}
