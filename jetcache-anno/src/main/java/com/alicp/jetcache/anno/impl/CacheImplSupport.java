/**
 * Created on  13-09-12 09:51
 */
package com.alicp.jetcache.anno.impl;

import com.alicp.jetcache.anno.support.CacheAnnoConfig;
import com.alicp.jetcache.support.FastjsonKeyGenerator;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;

import java.util.function.Function;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheImplSupport {

    public static Function<Object, Object> getDefaultKeyGenerator() {
        return FastjsonKeyGenerator.INSTANCE;
    }

    public static <T> T getProxy(T target, CacheAnnoConfig cacheAnnoConfig, GlobalCacheConfig globalCacheConfig) {
        return ProxyUtil.getProxy(target, cacheAnnoConfig, globalCacheConfig);
    }

    public static <T> T getProxyByAnnotation(T target, GlobalCacheConfig globalCacheConfig) {
        return ProxyUtil.getProxyByAnnotation(target, globalCacheConfig);
    }

    public static Object invoke(CacheInvokeContext context) throws Throwable {
        return CacheHandler.invoke(context);
    }

    public static byte[] encodeValue(Object value, String serialPolicy) throws Exception {
        return SerializeUtil.encode(value, serialPolicy);
    }

    public static Object decodeValue(byte[] buffer) throws Exception {
        return SerializeUtil.decode(buffer);
    }
}
