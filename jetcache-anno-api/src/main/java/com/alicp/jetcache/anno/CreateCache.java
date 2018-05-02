package com.alicp.jetcache.anno;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/12/9.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CreateCache {
    /**
     * If you want to use multi backend cache system, you can setup multi "cache area" in configuration,
     * this attribute specifies the name of the "cache area" you want to use.
     * @return the name of cache area
     */
    String area() default CacheConsts.DEFAULT_AREA;

    /**
     * The name of this Cache instance, optional. If you do not specify, JetCache will auto generate one.
     * The name is used to display statistics information and as part of key prefix when using a remote cache.
     * If two @CreateCache have same name and area, they will point to same Cache instance.
     * @return the cache name
     */
    String name() default CacheConsts.UNDEFINED_STRING;

    /**
     * Specify the time unit of expire.
     * @return the time unit of expire
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * The default expire time of this Cache instance.
     * Use global config if the attribute value is absent, and if the global config is not defined either,
     * use infinity.
     * @return the default expire time
     */
    int expire() default CacheConsts.UNDEFINED_INT;

    /**
     * Use to specify the local cache expire time when cacheType=CacheType.BOTH, use "expire" if absent.
     * @return the local cache expire time
     */
    int localExpire() default CacheConsts.UNDEFINED_INT;

    /**
     * Type of the Cache instance. May be CacheType.REMOTE, CacheType.LOCAL, CacheType.BOTH.
     * Use two level cache (local+remote) when value is CacheType.BOTH.
     * @return the cache type
     */
    CacheType cacheType() default CacheType.REMOTE;

    /**
     * Specify max elements in local memory when cacheType is CacheType.LOCAL or CacheType.BOTH.
     * Use global config if the attribute value is absent, and if the global config is not defined either,
     * use CacheConsts.DEFAULT_LOCAL_LIMIT instead.
     * @return local maximal elements of the LOCAL/BOTH cache
     */
    int localLimit() default CacheConsts.UNDEFINED_INT;

    /**
     * Specify the serialization policy of remote cache when cacheType is CacheType.REMOTE or CacheType.BOTH.
     * The JetCache build-in serialPolicy are SerialPolicy.JAVA or SerialPolicy.KRYO.
     * Use global config if the attribute value is absent, and if the global config is not defined either,
     * use SerialPolicy.JAVA instead.
     * @return the serialization policy name of cache value
     */
    String serialPolicy() default CacheConsts.UNDEFINED_STRING;

    /**
     * Specify the key convertor. Used to convert the complex key object.
     * The JetCache build-in keyConvertor are KeyConvertor.FASTJSON or KeyConvertor.NONE.
     * NONE indicate do not convert, FASTJSON will use fastjson to convert key object to a string.
     * Use global config if the attribute value is absent.
     * @return convertor name of cache key
     */
    String keyConvertor() default CacheConsts.UNDEFINED_STRING;
}
