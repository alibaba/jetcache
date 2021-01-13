package com.alicp.jetcache.support;

import java.util.concurrent.*;

/**
 * Created on 2017/5/3.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class JetCacheExecutor {
    protected static ScheduledExecutorService defaultExecutor;
    protected static ScheduledExecutorService heavyIOExecutor;

    private static int threadCount;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (defaultExecutor != null) {
                    defaultExecutor.shutdownNow();
                }
                if (heavyIOExecutor != null) {
                    heavyIOExecutor.shutdownNow();
                }
            }
        });
    }

    public static ScheduledExecutorService defaultExecutor() {
        if (defaultExecutor != null) {
            return defaultExecutor;
        }
        synchronized (JetCacheExecutor.class) {
            if (defaultExecutor == null) {
                ThreadFactory tf = r -> {
                    Thread t = new Thread(r, "JetCacheDefaultExecutor");
                    t.setDaemon(true);
                    return t;
                };
                defaultExecutor = new ScheduledThreadPoolExecutor(
                        1, tf, new ThreadPoolExecutor.DiscardPolicy());
            }
        }
        return defaultExecutor;
    }

    public static ScheduledExecutorService heavyIOExecutor() {
        if (heavyIOExecutor != null) {
            return heavyIOExecutor;
        }
        synchronized (JetCacheExecutor.class) {
            if (heavyIOExecutor == null) {
                ThreadFactory tf = r -> {
                    Thread t = new Thread(r, "JetCacheHeavyIOExecutor" + threadCount++);
                    t.setDaemon(true);
                    return t;
                };
                heavyIOExecutor = new ScheduledThreadPoolExecutor(
                        10, tf, new ThreadPoolExecutor.DiscardPolicy());
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
