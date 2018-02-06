/**
 * Created on  13-09-09 18:16
 */
package com.alicp.jetcache;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class CacheGetResult<V> extends CacheResult {
    private V value;
    private CacheValueHolder<V> holder;

    public static final CacheGetResult NOT_EXISTS_WITHOUT_MSG = new CacheGetResult(CacheResultCode.NOT_EXISTS, null, null);
    public static final CacheGetResult EXPIRED_WITHOUT_MSG = new CacheGetResult(CacheResultCode.EXPIRED, null ,null);

    public CacheGetResult(CacheResultCode resultCode, String message, CacheValueHolder<V> holder) {
        super(CompletableFuture.completedFuture(new ResultData(resultCode, message, holder)));
    }

    public CacheGetResult(CompletionStage<ResultData> future) {
        super(future);
    }

    public CacheGetResult(Throwable ex) {
        super(ex);
    }

    public V getValue() {
        waitForResult();
        return value;
    }

    @Override
    protected void fetchResultSuccess(ResultData resultData) {
        super.fetchResultSuccess(resultData);
        holder = (CacheValueHolder<V>) resultData.getOriginData();
        value = (V) unwrapValue(holder);
    }

    static Object unwrapValue(Object holder) {
        // if @Cached or @CacheCache change type from REMOTE to BOTH (or from BOTH to REMOTE),
        // during the dev/publish process, the value type which different application server put into cache server will be different
        // (CacheValueHolder<V> and CacheValueHolder<CacheValueHolder<V>>, respectively).
        // So we need correct the problem at here and in MultiLevelCache.unwrapHolder
        Object v = holder;
        while (v != null && v instanceof CacheValueHolder) {
            v = ((CacheValueHolder) v).getValue();
        }
        return v;
    }


    @Override
    protected void fetchResultFail(Throwable e) {
        super.fetchResultFail(e);
        value = null;
    }

    protected CacheValueHolder<V> getHolder() {
        waitForResult();
        return holder;
    }
}
