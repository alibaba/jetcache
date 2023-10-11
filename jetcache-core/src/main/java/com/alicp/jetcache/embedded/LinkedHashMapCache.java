/**
 * Created on  13-09-12 19:02
 */
package com.alicp.jetcache.embedded;

import com.alicp.jetcache.CacheResultCode;
import com.alicp.jetcache.CacheValueHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author huangli
 */
public class LinkedHashMapCache<K, V> extends AbstractEmbeddedCache<K, V> {

    private static Logger logger = LoggerFactory.getLogger(LinkedHashMapCache.class);

    public LinkedHashMapCache(EmbeddedCacheConfig<K, V> config) {
        super(config);
        addToCleaner();
    }

    protected void addToCleaner() {
        Cleaner.add(this);
    }

    @Override
    protected InnerMap createAreaCache() {
        return new LRUMap(config.getLimit());
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        if (clazz.equals(LinkedHashMap.class)) {
            return (T) innerMap;
        }
        throw new IllegalArgumentException(clazz.getName());
    }

    public void cleanExpiredEntry() {
        ((LRUMap) innerMap).cleanExpiredEntry();
    }

    final class LRUMap extends LinkedHashMap implements InnerMap {

        private final int max;
//        private final Object lockObj;
        private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

        public LRUMap(int max) {
            super((int) (max * 1.4f), 0.75f, true);
            this.max = max;
//            this.lockObj = lockObj;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > max;
        }

        void cleanExpiredEntry() {
            Lock lock = readWriteLock.writeLock();
            lock.lock();
            try{
                for (Iterator it = entrySet().iterator(); it.hasNext();) {
                    Map.Entry en = (Map.Entry) it.next();
                    Object value = en.getValue();
                    if (value != null && value instanceof CacheValueHolder) {
                        CacheValueHolder h = (CacheValueHolder) value;
                        if (System.currentTimeMillis() >= h.getExpireTime()) {
                            it.remove();
                        }
                    } else {
                        // assert false
                        if (value == null) {
                            logger.error("key " + en.getKey() + " is null");
                        } else {
                            logger.error("value of key " + en.getKey() + " is not a CacheValueHolder. type=" + value.getClass());
                        }
                    }
                }
            }finally {
                lock.unlock();
            }
        }

        @Override
        public Object getValue(Object key) {
            Lock lock = readWriteLock.readLock();
            lock.lock();
            try{
                return get(key);
            }finally {
                lock.unlock();
            }
        }

        @Override
        public Map getAllValues(Collection keys) {
            Lock lock = readWriteLock.readLock();
            lock.lock();
            Map values = new HashMap();
            try{
                for (Object key : keys) {
                    Object v = get(key);
                    if (v != null) {
                        values.put(key, v);
                    }
                }
            }finally {
                lock.unlock();
            }
            return values;
        }

        @Override
        public void putValue(Object key, Object value) {
            Lock lock = readWriteLock.writeLock();
            lock.lock();
            try{
                put(key, value);
            }finally {
                lock.unlock();
            }
        }

        @Override
        public void putAllValues(Map map) {
            Lock lock = readWriteLock.writeLock();
            lock.lock();
            try{
                Set<Map.Entry> set = map.entrySet();
                for (Map.Entry en : set) {
                    put(en.getKey(), en.getValue());
                }
            }finally {
                lock.unlock();
            }
        }

        @Override
        public boolean removeValue(Object key) {
            Lock lock = readWriteLock.writeLock();
            lock.lock();
            try{
                return remove(key) != null;
            }finally {
                lock.unlock();
            }
        }

        @Override
        public void removeAllValues(Collection keys) {
            Lock lock = readWriteLock.writeLock();
            lock.lock();
            try{
                for (Object k : keys) {
                    remove(k);
                }
            }finally {
                lock.unlock();
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean putIfAbsentValue(Object key, Object value) {
            Lock lock = readWriteLock.writeLock();
            lock.lock();
            try{
                CacheValueHolder h = (CacheValueHolder) get(key);
                if (h == null || parseHolderResult(h).getResultCode() == CacheResultCode.EXPIRED) {
                    put(key, value);
                    return true;
                } else {
                    return false;
                }
            }finally {
                lock.unlock();
            }
        }
    }


}

