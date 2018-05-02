package com.alicp.jetcache;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2017/5/24.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class MultiLevelCacheConfig<K, V> extends CacheConfig<K, V> {
    private List<Cache<K, V>> caches = new ArrayList<>();
    private boolean useExpireOfSubCache;

    @Override
    public MultiLevelCacheConfig clone() {
        MultiLevelCacheConfig copy = (MultiLevelCacheConfig) super.clone();
        if (caches != null) {
            copy.caches = new ArrayList(this.caches);
        }
        return copy;
    }

    public List<Cache<K, V>> getCaches() {
        return caches;
    }

    public void setCaches(List<Cache<K, V>> caches) {
        this.caches = caches;
    }

    public boolean isUseExpireOfSubCache() {
        return useExpireOfSubCache;
    }

    public void setUseExpireOfSubCache(boolean useExpireOfSubCache) {
        this.useExpireOfSubCache = useExpireOfSubCache;
    }
}
