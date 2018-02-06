package com.alicp.jetcache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Created on 2017/2/21.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class MultiGetResult<K, V> extends CacheResult {
    private Map<K, CacheGetResult<V>> values;

    public MultiGetResult(CompletionStage<ResultData> future) {
        super(future);
    }

    public MultiGetResult(CacheResultCode resultCode, String message, Map<K, CacheGetResult<V>> values) {
        super(CompletableFuture.completedFuture(new ResultData(resultCode, message, values)));
    }

    public MultiGetResult(Throwable e) {
        super(e);
    }

    public Map<K, CacheGetResult<V>> getValues() {
        waitForResult();
        return values;
    }

    @Override
    protected void fetchResultSuccess(ResultData resultData) {
        super.fetchResultSuccess(resultData);
        values = (Map<K, CacheGetResult<V>>) resultData.getOriginData();
    }

    @Override
    protected void fetchResultFail(Throwable e) {
        super.fetchResultFail(e);
        values = null;
    }

    public Map<K, V> unwrapValues() {
        waitForResult();
        if (values == null) {
            return null;
        }
        Map<K, V> m = new HashMap<>();
        values.entrySet().stream().forEach((en) -> {
            if (en.getValue().isSuccess()) {
                m.put(en.getKey(), en.getValue().getValue());
            }
        });
        return m;
    }
}
