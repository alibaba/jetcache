/**
 * Created on  13-09-12 09:51
 */
package com.taobao.geek.jetcache.impl;

import com.taobao.geek.jetcache.SerialPolicy;
import com.taobao.geek.jetcache.support.CacheConfig;
import com.taobao.geek.jetcache.support.DefaultKeyGenerator;
import com.taobao.geek.jetcache.support.GlobalCacheConfig;
import com.taobao.geek.jetcache.support.KeyGenerator;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheImplSupport {

    public static KeyGenerator getDefaultKeyGenerator() {
        return new DefaultKeyGenerator();
    }

    public static <T> T getProxy(T target, CacheConfig cacheConfig, GlobalCacheConfig globalCacheConfig) {
        return ProxyUtil.getProxy(target, cacheConfig, globalCacheConfig);
    }

    public static <T> T getProxyByAnnotation(T target, GlobalCacheConfig globalCacheConfig) {
        return ProxyUtil.getProxyByAnnotation(target, globalCacheConfig);
    }

    public static Object invoke(CacheInvokeContext context) throws Throwable {
        return CacheHandler.invoke(context);
    }

    public static byte[] encodeValue(Object value, SerialPolicy serialPolicy) throws Exception {
        return SerializeUtil.encode(value, serialPolicy);
    }

    public static Object decodeValue(byte[] buffer) throws Exception {
        return SerializeUtil.decode(buffer);
    }
}
