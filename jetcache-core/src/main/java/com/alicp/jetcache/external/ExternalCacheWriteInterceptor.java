package com.alicp.jetcache.external;

import com.alicp.jetcache.Cache;

import java.util.concurrent.TimeUnit;

/**
 * Callback invoked before an external cache write attempt is dispatched.
 * <p>
 * This hook is intended for observation, validation and side effects such as
 * logging, metrics or big-key detection. The provided {@link WriteContext} is
 * immutable, so implementations cannot mutate the outgoing write request or
 * change the cache write result through this API.
 * </p>
 *
 * Created on 2026/1/30.
 *
 * @author youjie_li
 */
public interface ExternalCacheWriteInterceptor {

    <K, V> void intercept(WriteContext<K, V> ctx);


    /**
     * Immutable metadata of a single external cache write attempt.
     */
    final class WriteContext<K, V> {

        public final Cache<K, V> cache;

        private final K keyObj;

        private final V valueObj;

        private final byte[] keyBytes;

        private final byte[] valueBytes;

        private final long expireAfterWrite;

        private final TimeUnit timeUnit;

        private final Op op;

        public enum Op {PUT, PUT_ALL, PUT_IF_ABSENT}

        public WriteContext(Cache<K, V> cache, K keyObj, V valueObj, byte[] keyBytes, byte[] valueBytes, long expireAfterWrite, TimeUnit timeUnit, Op op) {
            this.cache = cache;
            this.keyObj = keyObj;
            this.valueObj = valueObj;
            this.keyBytes = keyBytes;
            this.valueBytes = valueBytes;
            this.expireAfterWrite = expireAfterWrite;
            this.timeUnit = timeUnit;
            this.op = op;
        }

        public Cache<K, V> getCache() {
            return cache;
        }

        public K getKeyObj() {
            return keyObj;
        }

        public V getValueObj() {
            return valueObj;
        }

        public byte[] getKeyBytes() {
            return keyBytes;
        }

        public byte[] getValueBytes() {
            return valueBytes;
        }

        public long getExpireAfterWrite() {
            return expireAfterWrite;
        }

        public TimeUnit getTimeUnit() {
            return timeUnit;
        }

        public Op getOp() {
            return op;
        }
    }
}
