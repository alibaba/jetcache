/**
 * Created on  13-09-04
 */
package com.taobao.geek.cache;

import java.lang.annotation.*;

/**
 * @author yeli.hl
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cached {
    public abstract String area() default CacheConsts.DEFAULT_AREA;
    public abstract String keyPrefix() default CacheConsts.DEFAULT_KEY_PREFIX;
    public abstract boolean enabled() default CacheConsts.DEFAULT_ENABLED;
    public abstract int expire() default CacheConsts.DEFAULT_EXPIRE;
    public abstract CacheType cacheType() default CacheType.REMOTE;
    public abstract int localLimit() default CacheConsts.DEFAULT_LOCAL_LIMIT;
    public abstract int version() default 1;
}
