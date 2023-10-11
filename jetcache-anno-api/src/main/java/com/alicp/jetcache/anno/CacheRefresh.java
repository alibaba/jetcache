/**
 * Created on 2018/2/1.
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
public @interface CacheRefresh {

    int refresh();

    int stopRefreshAfterLastAccess() default CacheConsts.UNDEFINED_INT;

    int refreshLockTimeout() default CacheConsts.UNDEFINED_INT;

    TimeUnit timeUnit() default TimeUnit.SECONDS;

}
