package com.alicp.jetcache.support;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created on 2017/5/3.
 *
 * @author huangli
 */
public class JetCacheExecutor {
    protected volatile static ScheduledExecutorService defaultExecutor;
    protected volatile static ScheduledExecutorService heavyIOExecutor;
    private static final ReentrantLock reentrantLock = new ReentrantLock();

    private static final AtomicInteger threadCount = new AtomicInteger(0);

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
        reentrantLock.lock();
        try{
            if (defaultExecutor == null) {
                ThreadFactory tf = r -> {
                    Thread t = new Thread(r, "JetCacheDefaultExecutor");
                    t.setDaemon(true);

                    ClassLoader classLoader = JetCacheExecutor.class.getClassLoader();
                    if (classLoader == null) {
                        // This class was loaded by the Bootstrap ClassLoader,
                        // so let's tie the thread's context ClassLoader to the System ClassLoader instead.
                        classLoader = ClassLoader.getSystemClassLoader();
                    }
                    t.setContextClassLoader(classLoader);

                    return t;
                };
                int coreSize = Math.min(4, Runtime.getRuntime().availableProcessors());
                defaultExecutor = new ScheduledThreadPoolExecutor(coreSize, tf);
            }
        }finally {
            reentrantLock.unlock();
        }
        return defaultExecutor;
    }

    public static ScheduledExecutorService heavyIOExecutor() {
        if (heavyIOExecutor != null) {
            return heavyIOExecutor;
        }
        reentrantLock.lock();
        try {
            if (heavyIOExecutor == null) {
                ThreadFactory tf = r -> {
                    Thread t = new Thread(r, "JetCacheHeavyIOExecutor" + threadCount.getAndIncrement());
                    t.setDaemon(true);
                    ClassLoader classLoader = JetCacheExecutor.class.getClassLoader();
                    if (classLoader == null) {
                        // This class was loaded by the Bootstrap ClassLoader,
                        // so let's tie the thread's context ClassLoader to the System ClassLoader instead.
                        classLoader = ClassLoader.getSystemClassLoader();
                    }
                    t.setContextClassLoader(classLoader);
                    return t;
                };
                heavyIOExecutor = new ScheduledThreadPoolExecutor(
                        10, tf, new ThreadPoolExecutor.DiscardPolicy());
            }
        }finally {
            reentrantLock.unlock();
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
