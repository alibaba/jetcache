/**
 * Created on  13-09-12 19:02
 */
package com.alicp.jetcache.embedded;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class LinkedHashMapCache extends AbstractEmbeddedCache {

    public LinkedHashMapCache(EmbeddedCacheConfig config){
        super(config);
    }

    @Override
    protected IntenalMap createAreaCache(){
        return new LRUMap(config.getLimit());
    }

    private static final class LRUMap extends LinkedHashMap implements IntenalMap {

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

