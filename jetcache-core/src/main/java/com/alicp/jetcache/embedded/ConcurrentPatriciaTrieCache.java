package com.alicp.jetcache.embedded;

import com.alicp.jetcache.*;
import org.apache.commons.collections4.trie.PatriciaTrie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentPatriciaTrieCache<K, V> extends AbstractEmbeddedCache<K, V> {
    private static Logger logger = LoggerFactory.getLogger(ConcurrentPatriciaTrieCache.class);

    public ConcurrentPatriciaTrieCache(EmbeddedCacheConfig<K, V> config) {
        super(config);
        addToCleaner();
    }

    @Override
    protected InnerMap createAreaCache() {
        return new ConcurrentPatriciaTrieCache.LRUMap(config.getLimit());
    }


    protected void addToCleaner() {
        Cleaner.add(this);
    }


    @Override
    public boolean delByPrefix(K keyPrefix) {
        Object realKeyPrefix = buildKey(keyPrefix);
        PatriciaTrie unwrap = this.unwrap(PatriciaTrie.class);
        SortedMap sortedMap = unwrap.prefixMap(realKeyPrefix);
        Set set = sortedMap.keySet();
        this.removeAll(set);
        return true;
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        if (clazz.equals(PatriciaTrie.class)) {
            return (T) innerMap;
        }
        throw new IllegalArgumentException(clazz.getName());
    }

    public void cleanExpiredEntry() {
        ((ConcurrentPatriciaTrieCache.LRUMap) innerMap).cleanExpiredEntry();
    }

    final class LRUMap extends PatriciaTrie implements ExtraInnerMap {

        private final int max;
        private final ReentrantLock lock = new ReentrantLock();

        public LRUMap(int max) {
            this.max = max;
        }

        @Override
        public SortedMap prefixMap(Object key) {
            lock.lock();
            try {
                return super.prefixMap(key);
            } finally {
                lock.unlock();
            }
        }

        //todo @Override
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > max;
        }

        void cleanExpiredEntry() {
            lock.lock();
            long t = System.currentTimeMillis();
            try {
                for (Iterator it = entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry en = (Map.Entry) it.next();
                    Object value = en.getValue();
                    if (value != null) {
                        CacheValueHolder h;
                        try {
                            h = (CacheValueHolder) value;
                        } catch (ClassCastException e) {
                            // assert false
                            logger.error("value of key " + en.getKey() + " is not a CacheValueHolder. type=" + value.getClass());
                            it.remove();
                            continue;
                        }
                        if (t >= h.getExpireTime()) {
                            it.remove();
                        }
                    } else {
                        // assert false
                        logger.error("key " + en.getKey() + " is null");
                    }
                }
            } finally {
                lock.unlock();
            }
        }

        @Override
        public Object getValue(Object key) {
            lock.lock();
            try {
                return get(key);
            } finally {
                lock.unlock();
            }
        }

        @Override
        public Map getAllValues(Collection keys) {
            lock.lock();
            Map values = new HashMap();
            try {
                for (Object key : keys) {
                    Object v = get(key);
                    if (v != null) {
                        values.put(key, v);
                    }
                }
            } finally {
                lock.unlock();
            }
            return values;
        }

        @Override
        public void putValue(Object key, Object value) {
            lock.lock();
            try {
                put(key, value);
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void putAllValues(Map map) {
            lock.lock();
            try {
                Set<Map.Entry> set = map.entrySet();
                for (Map.Entry en : set) {
                    put(en.getKey(), en.getValue());
                }
            } finally {
                lock.unlock();
            }
        }

        @Override
        public boolean removeValue(Object key) {
            lock.lock();
            try {
                return remove(key) != null;
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void removeAllValues(Collection keys) {
            lock.lock();
            try {
                for (Object k : keys) {
                    remove(k);
                }
            } finally {
                lock.unlock();
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean putIfAbsentValue(Object key, Object value) {
            lock.lock();
            try {
                CacheValueHolder h = (CacheValueHolder) get(key);
                if (h == null || parseHolderResult(h).getResultCode() == CacheResultCode.EXPIRED) {
                    put(key, value);
                    return true;
                } else {
                    return false;
                }
            } finally {
                lock.unlock();
            }
        }
    }

}
