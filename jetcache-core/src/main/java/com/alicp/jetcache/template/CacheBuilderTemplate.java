/**
 * Created on 2022/07/30.
 */
package com.alicp.jetcache.template;

import com.alicp.jetcache.CacheBuilder;

import java.util.Map;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class CacheBuilderTemplate {
    private final boolean penetrationProtect;
    private final Map<String, CacheBuilder>[] cacheBuilders;

    @SafeVarargs
    public CacheBuilderTemplate(boolean penetrationProtect, Map<String, CacheBuilder>... cacheBuilders) {
        this.penetrationProtect = penetrationProtect;
        this.cacheBuilders = cacheBuilders;
    }

    public boolean isPenetrationProtect() {
        return penetrationProtect;
    }

    public CacheBuilder getCacheBuilder(int level, String area) {
        return cacheBuilders[level].get(area);
    }

}
