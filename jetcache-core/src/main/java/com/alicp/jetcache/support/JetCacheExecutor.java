package com.alicp.jetcache.support;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Created on 2017/5/3.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class JetCacheExecutor {
    protected static ScheduledExecutorService executorService;

    public static ScheduledExecutorService defaultExecutor() {
        if (executorService != null) {
            return executorService;
        }
        initExecutor();
        return executorService;
    }

    private static void initExecutor() {
        synchronized (DefaultCacheMonitor.class) {
            if (executorService == null) {
                executorService = new ScheduledThreadPoolExecutor(10, r -> {
                    Thread t = new Thread(r, "JetCacheExecutorThread");
                    t.setDaemon(true);
                    return t;
                });
            }
        }
    }

    public static void setDefaultExecutor(ScheduledExecutorService executor) {
        JetCacheExecutor.executorService = executor;
    }
}
