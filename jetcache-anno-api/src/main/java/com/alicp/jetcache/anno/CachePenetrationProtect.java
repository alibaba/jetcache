/**
 * Created on 2018/4/27.
 */
package com.alicp.jetcache.anno;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * @author huangli
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface CachePenetrationProtect {
    boolean value() default true;
    int timeout() default CacheConsts.UNDEFINED_INT;
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
