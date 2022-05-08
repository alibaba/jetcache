/**
 * Created on 2022/5/7.
 */
package com.alicp.jetcache.support;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.CacheUtil;
import com.alicp.jetcache.MultiLevelCache;
import com.alicp.jetcache.embedded.AbstractEmbeddedCache;

import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class CacheMessageConsumer implements Consumer<CacheMessage> {
    private final String sourceId;
    private final CacheManager cacheManager;

    public CacheMessageConsumer(String sourceId, CacheManager cacheManager) {
        this.sourceId = sourceId;
        this.cacheManager = cacheManager;
    }

    @Override
    public void accept(CacheMessage cacheMessage) {
        if (cacheMessage == null) {
            return;
        }
        if (sourceId.equals(cacheMessage.getSourceId())) {
            return;
        }
        Cache cache = cacheManager.getCache(cacheMessage.getArea(), cacheMessage.getCacheName());
        Cache absCache = CacheUtil.getAbstractCache(cache);
        if (!(absCache instanceof MultiLevelCache)) {
            return;
        }
        Cache[] caches = ((MultiLevelCache) absCache).caches();
        Set<Object> keys = Stream.of(cacheMessage.getKeys()).collect(Collectors.toSet());
        for (Cache c : caches) {
            Cache localCache = CacheUtil.getAbstractCache(c);
            if (localCache instanceof AbstractEmbeddedCache) {
                localCache.REMOVE_ALL(keys);
            } else {
                break;
            }
        }
    }
}
