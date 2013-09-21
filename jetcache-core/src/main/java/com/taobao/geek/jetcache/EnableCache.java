/**
 * Created on  13-09-04
 */
package com.taobao.geek.jetcache;

import java.lang.annotation.*;

/**
 * @author yeli.hl
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EnableCache {
}
