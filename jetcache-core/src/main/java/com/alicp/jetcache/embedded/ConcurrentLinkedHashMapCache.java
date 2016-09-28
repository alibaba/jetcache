/**
 * Created on  13-10-20 01:53
 */
package com.alicp.jetcache.embedded;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

import java.util.concurrent.ConcurrentMap;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class ConcurrentLinkedHashMapCache extends AbstractEmbeddedCache {

    public ConcurrentLinkedHashMapCache(EmbeddedCacheConfig config){
        super(config);
    }

    @Override
    protected AreaCache createAreaCache() {
        return new ConcurrentAreaCache(config.getLimit());
    }

    private static class ConcurrentAreaCache implements AreaCache {
        ConcurrentMap<Object, Object> cache;

        ConcurrentAreaCache(int limit) {
            cache = new ConcurrentLinkedHashMap.Builder()
                    .maximumWeightedCapacity(limit)
                    .initialCapacity(limit)
                    .build();
        }

        public Object getValue(Object key) {
            return cache.get(key);
        }

        public Object putValue(Object key, Object value) {
            return cache.put(key, value);
        }

        public Object removeValue(Object key) {
            return cache.remove(key);
        }
    }

}
