package com.alicp.jetcache.anno;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/12/9.
 *
 * @deprecated replaced by CacheManager.getOrCreateCache(QuickConfig), the CacheManager instance
 *             can be injected use annotation such as @Autowired.
 *
 * @author huangli
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Deprecated
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
     * Use to specify the local cache expire time when cacheType=CacheType.BOTH.
     * If not set, use "expire" as local expire time. If the global config
     * "useDefaultLocalExpireInMultiLevelCache" is set to true, use the local cache
     * builder's expireAfterWriteInMillis instead (but not greater than "expire").
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
     * if cacheType is CacheType.BOTH and the remote cache supports broadcast (or BroadcastManager bean exists),
     * invalidate local cache of all process after put/remove operation.
     * @return should sync local cache
     */
    boolean syncLocal() default false;

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

    /**
     * Specify external cache write pre-hooks for this cache.
     * Multiple interceptor bean references can be separated by commas.
     * In annotation-based configuration, interceptor beans must be Spring beans
     * referenced with the {@code bean:} prefix, for example
     * {@code bean:loggingInterceptor,bean:auditInterceptor}.
     * <p>
     * The hook runs before REMOTE/BOTH cache write attempts and receives an
     * read-only write metadata view. It is intended for observation or
     * validation, not for mutating the outgoing encoded write request.
     * Throwing an exception from the hook causes the current write operation
     * to fail through its cache result instead of propagating from the base
     * write methods.
     * </p>
     * <p>
     * This attribute is only valid for external caches (REMOTE or BOTH cache types).
     * Using it with LOCAL cache type is a configuration error.
     * </p>
     *
     * @return external cache write interceptor bean references
     */
    String externalWriteInterceptors() default CacheConsts.UNDEFINED_STRING;
}
