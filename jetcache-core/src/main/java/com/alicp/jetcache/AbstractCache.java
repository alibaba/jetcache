package com.alicp.jetcache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created on 2016/10/7.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public abstract class AbstractCache<K, V> implements Cache<K, V> {

    private static Logger logger = LoggerFactory.getLogger(AbstractCache.class);

    protected abstract Object buildKey(K key);

    /*
    protected abstract CacheGetResult<CacheValueHolder<V>> getHolder(K key);

    protected abstract MultiGetResult<K, CacheValueHolder<V>> getHolder(Set<? extends K> keys);

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
    public MultiGetResult<K, V> GET_ALL(Set<? extends K> keys) {
        if (keys == null) {
            return new MultiGetResult<>(CacheResultCode.FAIL, CacheResult.MSG_ILLEGAL_ARGUMENT, null);
        }
        try {
            MultiGetResult<K, CacheValueHolder<V>> holders = getHolder(keys);
            if (holders.getValues() != null) {
                Set<Map.Entry<K, CacheGetResult<CacheValueHolder<V>>>> entries = holders.getValues().entrySet();
                for (Map.Entry<K, CacheGetResult<CacheValueHolder<V>>> e : entries) {
                    CacheGetResult<CacheValueHolder<V>> r = e.getValue();
                    V v = r.getValue().getValue();
                    ((CacheGetResult) r).setValue(v);
                }
            }
            return (MultiGetResult<K, V>) holders;
        } catch (Exception ex) {
            logError("GET", "keys(" + keys.size() + ")", ex);
            return new MultiGetResult<>(ex);
        }
    }
    */

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
