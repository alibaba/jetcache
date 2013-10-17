/**
 * Created on  13-09-12 19:02
 */
package com.taobao.geek.jetcache.local;

import com.taobao.geek.jetcache.support.CacheConfig;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class LinkedHashMapCache extends AbstractLocalCache {

    public LinkedHashMapCache(){
    }

    public LinkedHashMapCache(boolean useSoftRef){
        super(useSoftRef);
    }

    @Override
    protected AreaCache getCacheMap(CacheConfig cacheConfig, String subArea) {
        StringBuilder sb = new StringBuilder();
        sb.append(cacheConfig.getArea());
        sb.append('_');
        sb.append(subArea);
        String areaKey = sb.toString();
        AreaCache areaCache = areaMap.get(areaKey);
        if (areaCache == null) {
            areaCache = new LRUMap(cacheConfig.getLocalLimit());
            areaMap.put(areaKey, areaCache);
        }
        return areaCache;
    }

    private static final class LRUMap extends LinkedHashMap implements AreaCache {

        private final int max;

        public LRUMap(int max) {
            super((int) (max * 1.4f), 0.75f, true);
            this.max = max;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > max;
        }

        @Override
        public synchronized Object getValue(String key) {
            return get(key);
        }

        @Override
        public synchronized Object putValue(String key, Object value) {
            return put(key, value);
        }

        @Override
        public synchronized Object removeValue(String key) {
            return remove(key);
        }
    }


}

