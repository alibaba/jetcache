package com.alicp.jetcache.anno;

import java.lang.annotation.*;

/**
 * Created on 2016/12/9.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CreateCache {
    String area() default CacheConsts.DEFAULT_AREA;
    String name() default CacheConsts.DEFAULT_NAME;
    int expire() default CacheConsts.DEFAULT_EXPIRE;
    CacheType cacheType() default CacheType.REMOTE;
    int localLimit() default CacheConsts.DEFAULT_LOCAL_LIMIT;
    int version() default CacheConsts.DEFAULT_VERSION;
    String serialPolicy() default CacheConsts.DEFAULT_SERIAL_POLICY;

    String keyConvertor() default KeyConvertor.FASTJSON;
}
