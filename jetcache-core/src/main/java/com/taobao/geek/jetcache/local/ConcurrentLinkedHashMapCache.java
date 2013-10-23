/**
 * Created on  13-10-20 01:53
 */
package com.taobao.geek.jetcache.local;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

import java.util.concurrent.ConcurrentMap;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class ConcurrentLinkedHashMapCache extends AbstractLocalCache {
    public ConcurrentLinkedHashMapCache(){
    }

    public ConcurrentLinkedHashMapCache(boolean useSoftRef){
        super(useSoftRef);
    }

    @Override
    protected AreaCache createAreaCache(int localLimit) {
        return new ConcurrentAreaCache(localLimit);
    }

    private static class ConcurrentAreaCache implements AreaCache {
        ConcurrentMap<String, Object> cache;

        ConcurrentAreaCache(int limit) {
            cache = new ConcurrentLinkedHashMap.Builder<String, Object>()
                    .maximumWeightedCapacity(limit)
                    .initialCapacity(limit)
                    .build();
        }

        @Override
        public Object getValue(String key) {
            return cache.get(key);
        }

        @Override
        public Object putValue(String key, Object value) {
            return cache.put(key, value);
        }

        @Override
        public Object removeValue(String key) {
            return cache.remove(key);
        }
    }

}
