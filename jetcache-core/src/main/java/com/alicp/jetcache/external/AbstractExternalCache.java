package com.alicp.jetcache.external;

import com.alicp.jetcache.AbstractCache;
import com.alicp.jetcache.CacheConfigException;
import com.alicp.jetcache.CacheException;
import com.alicp.jetcache.CacheResult;
import com.alicp.jetcache.RefreshCache;
import com.alicp.jetcache.anno.KeyConvertor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.alicp.jetcache.CacheResultCode.FAIL;

/**
 * Created on 2016/10/8.
 *
 * @author huangli
 */
public abstract class AbstractExternalCache<K, V> extends AbstractCache<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractExternalCache.class);

    private ExternalCacheConfig<K, V> config;

    public AbstractExternalCache(ExternalCacheConfig<K, V> config) {
        this.config = config;
        checkConfig();
    }

    protected void checkConfig() {
        if (config.getValueEncoder() == null) {
            throw new CacheConfigException("no value encoder");
        }
        if (config.getValueDecoder() == null) {
            throw new CacheConfigException("no value decoder");
        }
        if (config.getKeyPrefix() == null) {
            throw new CacheConfigException("keyPrefix is required");
        }
    }

    public byte[] buildKey(K key) {
        try {
            Object newKey = key;
            if (config.getKeyConvertor() != null) {
                if (config.getKeyConvertor() instanceof KeyConvertor) {
                    if (!isPreservedKey(key)) {
                        // since 2.7.3 KeyConvertor extends Function<Object, Object>
                        newKey = config.getKeyConvertor().apply(key);
                    }
                } else {
                    // before 2.7.3, KeyConvertor is interface only place some constants.
                    // "key convertor" is Function<Object, Object> and can't process byte[] and String
                    if (key instanceof byte[]) {
                        newKey = key;
                    } else if (key instanceof String) {
                        newKey = key;
                    } else {
                        newKey = config.getKeyConvertor().apply(key);
                    }
                }
            }
            return ExternalKeyUtil.buildKeyAfterConvert(newKey, config.getKeyPrefix());
        } catch (IOException e) {
            throw new CacheException(e);
        }
    }

    private boolean isPreservedKey(Object key) {
        if (key instanceof byte[]) {
            byte[] keyBytes = (byte[]) key;
            return endWith(keyBytes, RefreshCache.LOCK_KEY_SUFFIX)
                    || endWith(keyBytes, RefreshCache.TIMESTAMP_KEY_SUFFIX);
        }
        return false;
    }

    private boolean endWith(byte[] key, byte[] suffix) {
        int len = suffix.length;
        if (key.length < len) {
            return false;
        }
        int startPos = key.length - len;
        for (int i = 0; i < len; i++) {
            if (key[startPos + i] != suffix[i]) {
                return false;
            }
        }
        return true;
    }

    protected CacheResult interceptWrite(K key, V value, byte[] keyBytes, byte[] valueBytes,
                                         long expireAfterWrite, TimeUnit timeUnit, ExternalCacheWriteInterceptor.WriteContext.Op op) {
        ExternalCacheWriteInterceptor.WriteContext<K, V> ctx = new ExternalCacheWriteInterceptor.WriteContext<>(
                this, key, value, keyBytes, valueBytes, expireAfterWrite, timeUnit, op);
        return interceptWrite(ctx);
    }

    private CacheResult interceptWrite(ExternalCacheWriteInterceptor.WriteContext<K, V> ctx) {
        List<ExternalCacheWriteInterceptor> list = config.getWriteInterceptors();
        if (list == null || list.isEmpty()) {
            return null;
        }

        for (ExternalCacheWriteInterceptor it : list) {
            try {
                ExternalCacheWriteInterceptor.WriteInterceptDecision decision = it.intercept(ctx);
                if (decision == null) {
                    IllegalStateException e = new IllegalStateException("ExternalCacheWriteInterceptor returned null");
                    logError("WRITE_INTERCEPT", ctx.getKeyObj(), e);
                    return new CacheResult(e);
                }
                if (decision.isReject()) {
                    String message = renderRejectMessage(decision);
                    logger.warn("jetcache({}) WRITE_INTERCEPT reject. key=[{}], message=[{}]", this.getClass().getSimpleName(), ctx.getKeyObj(), message);
                    return new CacheResult(FAIL, message);
                }
            } catch (Exception e) {
                logError("WRITE_INTERCEPT", ctx.getKeyObj(), e);
                return new CacheResult(e);
            }
        }
        return null;
    }

    private String renderRejectMessage(ExternalCacheWriteInterceptor.WriteInterceptDecision decision) {
        String code = decision.getCode();
        String message = decision.getMessage();
        if (code == null || code.trim().isEmpty()) {
            return message == null || message.trim().isEmpty() ? "write rejected by interceptor" : message;
        }
        if (message == null || message.trim().isEmpty()) {
            return code;
        }
        return code + ": " + message;
    }


}
