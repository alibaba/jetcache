/**
 * Created on 2018/3/22.
 */
package com.alicp.jetcache.anno.support;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public interface CacheNameGenerator {

    String generateCacheName(Method method, Object targetObject);

    String generateCacheName(Field field);
}
