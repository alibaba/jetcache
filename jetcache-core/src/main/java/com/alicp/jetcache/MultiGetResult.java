package com.alicp.jetcache;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 2017/2/21.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class MultiGetResult<K, V> extends CacheResult {
    private Map<K, CacheGetResult<V>> values;

    public MultiGetResult(CacheResultCode resultCode, String message, Map<K, CacheGetResult<V>> values) {
        super(resultCode, message);
        this.values = values;
    }

    public MultiGetResult(Throwable e) {
        super(e);
    }

    public Map<K, CacheGetResult<V>> getValues() {
        return values;
    }

    public void setValues(Map<K, CacheGetResult<V>> values) {
        this.values = values;
    }

    public Map<K, V> unwrapValues() {
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
