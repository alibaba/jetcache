/**
 * Created on 2018/4/27.
 */
package com.alicp.jetcache.anno;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface CachePenetrationProtect {
    boolean value() default true;
    int timeout() default CacheConsts.UNDEFINED_INT;
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
