/**
 * Created on 2018/1/23.
 */
package com.alicp.jetcache.anno;

import java.lang.annotation.*;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CacheUpdate {
    /**
     * If you want to use multi backend cache system, you can setup multi "cache area" in configuration,
     * this attribute specifies the name of the "cache area" you want to use.
     * @return the name of cache area
     */
    String area() default CacheConsts.DEFAULT_AREA;

    /**
     * The name of this Cache instance which need a update operation.
     * @return the name of the cache which need a update operation
     */
    String name();

    /**
     * Specify the key by expression script, optional. If not specified,
     * use all parameters of the target method and keyConvertor to generate one.
     * @return an expression script which specifies key
     */
    String key() default CacheConsts.UNDEFINED_STRING;

    /**
     * Specify the cache value by expression script.
     * @return an expression script which specifies cache value
     */
    String value();

    /**
     * Expression script used for conditioning the cache operation, the operation is vetoed when evaluation result is false.
     * Evaluation occurs after real method invocation so we can refer <code>#result</code> in script.
     */
    String condition() default CacheConsts.UNDEFINED_STRING;

    /**
     * If both evaluated key and value are array or instance of java.lang.Iterable,
     * set multi to true indicates jetcache to update K/V pair to cache instead of update single converted K/V.
     */
    boolean multi() default CacheConsts.DEFAULT_MULTI;
}
