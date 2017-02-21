package com.alicp.jetcache;

import java.util.Map;

/**
 * Created on 2017/2/21.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class MultiOpResult<K> extends CacheResult {

    private Map<K, CacheResult> results;

    public MultiOpResult(CacheResultCode resultCode, String message, Map<K, CacheResult> results) {
        super(resultCode, message);
        this.results = results;
    }

    public Map<K, CacheResult> getResults() {
        return results;
    }

    public void setResults(Map<K, CacheResult> results) {
        this.results = results;
    }
}
