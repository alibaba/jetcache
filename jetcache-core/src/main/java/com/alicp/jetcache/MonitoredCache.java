package com.alicp.jetcache;

import java.util.Arrays;
import java.util.Objects;

/**
 * Created on 2016/10/27.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
@Deprecated
public class MonitoredCache<K, V> extends SimpleProxyCache<K, V> {

    private CacheMonitor[] monitors;

    public MonitoredCache(Cache<K, V> cache, CacheMonitor... monitors) {
        super(cache);
        this.monitors = monitors;
        Objects.requireNonNull(monitors);
        cache.config().getMonitors().addAll(Arrays.asList(monitors));
    }

    public CacheMonitor[] getMonitors() {
        return monitors;
    }

}
