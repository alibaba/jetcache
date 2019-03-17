package com.alicp.jetcache.support;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created on 2017/5/3.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class JetCacheExecutor {
    protected static ScheduledExecutorService defaultExecutor;
    protected static ScheduledExecutorService heavyIOExecutor;

    private static int threadCount;

    @SuppressWarnings("PMD.ThreadPoolCreationRule")
    public static ScheduledExecutorService defaultExecutor() {
        if (defaultExecutor != null) {
            return defaultExecutor;
        }
        synchronized (JetCacheExecutor.class) {
            if (defaultExecutor == null) {
                defaultExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
                    Thread t = new Thread(r, "JetCacheDefaultExecutor");
                    t.setDaemon(true);
                    return t;
                });
            }
        }
        return defaultExecutor;
    }

    @SuppressWarnings("PMD.ThreadPoolCreationRule")
    public static ScheduledExecutorService heavyIOExecutor() {
        if (heavyIOExecutor != null) {
            return heavyIOExecutor;
        }
        synchronized (JetCacheExecutor.class) {
            if (heavyIOExecutor == null) {
                heavyIOExecutor = Executors.newScheduledThreadPool(10, r -> {
                    Thread t = new Thread(r, "JetCacheHeavyIOExecutor" + threadCount++);
                    t.setDaemon(true);
                    return t;
                });
            }
        }
        return heavyIOExecutor;
    }

    public static void setDefaultExecutor(ScheduledExecutorService executor) {
        JetCacheExecutor.defaultExecutor = executor;
    }

    public static void setHeavyIOExecutor(ScheduledExecutorService heavyIOExecutor) {
        JetCacheExecutor.heavyIOExecutor = heavyIOExecutor;
    }
}
