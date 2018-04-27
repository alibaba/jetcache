/**
 * Created on 2018/4/27.
 */
package com.alicp.jetcache.anno;

import java.lang.annotation.*;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface CachePenetrationProtect {
    boolean value() default true;
}
