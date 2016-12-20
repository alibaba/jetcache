/**
 * Created on  13-09-12 19:02
 */
package com.alicp.jetcache.embedded;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class LinkedHashMapCache<K, V> extends AbstractEmbeddedCache<K, V> {


    public LinkedHashMapCache(EmbeddedCacheConfig config){
        super(config);
    }

    @Override
    protected InnerMap createAreaCache(){
        return new LRUMap(config.getLimit());
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        if(clazz.equals(LinkedHashMap.class)){
            return (T) innerMap;
        }
        throw new IllegalArgumentException(clazz.getName());
    }

    private static final class LRUMap extends LinkedHashMap implements InnerMap {

        private final int max;

        public LRUMap(int max) {
            super((int) (max * 1.4f), 0.75f, true);
            this.max = max;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > max;
        }

        public synchronized Object getValue(Object key) {
            return get(key);
        }

        public synchronized void putValue(Object key, Object value) {
            put(key, value);
        }

        public synchronized boolean removeValue(Object key) {
            return remove(key) != null;
        }
    }


}

