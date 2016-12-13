package com.alicp.jetcache.ali.tair;

import com.alicp.jetcache.external.ExternalCacheConfig;
import com.taobao.tair.TairManager;

/**
 * Created on 16/9/13.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class TairCacheConfig extends ExternalCacheConfig {
    private int namespace;
    private TairManager tairManager;

    public int getNamespace() {
        return namespace;
    }

    public void setNamespace(int namespace) {
        this.namespace = namespace;
    }

    public TairManager getTairManager() {
        return tairManager;
    }

    public void setTairManager(TairManager tairManager) {
        this.tairManager = tairManager;
    }
}
