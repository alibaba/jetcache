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
public @interface EnableCache {
}
