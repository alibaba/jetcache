package com.alicp.jetcache;

import com.alicp.jetcache.support.CacheEncodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created on 2016/10/7.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public abstract class AbstractCache<K, V> implements Cache<K, V> {

    private static Logger logger = LoggerFactory.getLogger(AbstractCache.class);

    protected abstract Object buildKey(K key);

    protected abstract CacheGetResult<CacheValueHolder<V>> getHolder(K key);

    protected abstract List<CacheGetResult<CacheValueHolder<V>>> getHolder(List<? extends K> keys);

    @Override
    public CacheGetResult<V> GET(K key) {
        CacheGetResult<V> result;
        try {
            CacheGetResult<CacheValueHolder<V>> holderResult = getHolder(key);
            result = (CacheGetResult<V>) holderResult;
            if (holderResult.getValue() != null) {
                result.setValue(holderResult.getValue().getValue());
            }
        } catch (Exception ex) {
            logError("GET", key, ex);
            result = new CacheGetResult<>(CacheResultCode.FAIL, ex.getClass() + ":" + ex.getMessage(), null);
        }
        return result;
    }

    @Override
    public List<CacheGetResult<V>> GET_ALL(List<? extends K> keys) {
        try {
            List holders = getHolder(keys);
            for (int i = 0; i < holders.size(); i++) {
                CacheGetResult<CacheValueHolder<V>> r = (CacheGetResult<CacheValueHolder<V>>) holders.get(i);

            }
        } catch (Exception ex) {
            logError("GET", "keys(" + keys.size() + ")", ex);
            CacheGetResult<Object> getResult = new CacheGetResult<>(CacheResultCode.FAIL, ex.getClass() + ":" + ex.getMessage(), null);
        }
    }

    protected void logError(String oper, Object key, Throwable e) {
        StringBuilder sb = new StringBuilder(64);
        sb.append("jetcache(")
                .append(this.getClass().getSimpleName()).append(") ")
                .append(oper)
                .append(" error. key=")
                .append(key)
                .append(".");
        if (needLogStackTrace(e)) {
            logger.error(sb.toString(), e);
        } else {
            sb.append(" Exception=").append(e.getClass()).append(", Message:").append(e.getMessage());
            logger.warn(sb.toString());
        }

    }

    protected boolean needLogStackTrace(Throwable e) {
//        if (e instanceof CacheEncodeException) {
//            return true;
//        }
//        return false;
        return true;
    }
}
