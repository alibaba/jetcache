/**
 * Created on  13-09-09 18:16
 */
package com.alicp.jetcache;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheGetResult<V> extends CacheResult {
    private V value;

    public static final CacheGetResult NOT_EXISTS_WITHOUT_MSG = new CacheGetResult(CacheResultCode.NOT_EXISTS, null, null);
    public static final CacheGetResult EXPIRED_WITHOUT_MSG = new CacheGetResult(CacheResultCode.EXPIRED, null ,null);

    public CacheGetResult(CacheResultCode resultCode, String message, V value) {
        super(CompletableFuture.completedFuture(new ResultData(resultCode, message, value)));
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
        value = (V) resultData.getData();
    }

    @Override
    protected void fetchResultFail(Throwable e) {
        super.fetchResultFail(e);
        value = null;
    }
}
