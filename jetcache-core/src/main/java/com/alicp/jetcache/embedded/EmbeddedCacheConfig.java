package com.alicp.jetcache.embedded;

import com.alicp.jetcache.anno.CacheConsts;
import com.alicp.jetcache.CacheConfig;

/**
 * Created on 16/9/7.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class EmbeddedCacheConfig extends CacheConfig {
    private int limit = CacheConsts.DEFAULT_LOCAL_LIMIT;
    private boolean useSoftRef = false;

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public boolean isUseSoftRef() {
        return useSoftRef;
    }

    public void setUseSoftRef(boolean useSoftRef) {
        this.useSoftRef = useSoftRef;
    }
}
