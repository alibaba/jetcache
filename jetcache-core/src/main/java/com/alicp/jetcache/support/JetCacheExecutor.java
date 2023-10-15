package com.alicp.jetcache.support;

import java.util.concurrent.*;
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
                if(VirtualThreadUtil.isVirtualThreadSupported()) {
                    defaultExecutor = new VirtualScheduledThreadPoolExecutor("JetCacheDefaultExecutor");
                }else {
                    defaultExecutor = new ScheduledThreadPoolExecutor(
                            1, VirtualThreadUtil.createThreadFactory(false, "JetCacheDefaultExecutor-")
                            , new ThreadPoolExecutor.DiscardPolicy());
                }
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
                if(VirtualThreadUtil.isVirtualThreadSupported()) {
                    heavyIOExecutor = new VirtualScheduledThreadPoolExecutor("JetCacheHeavyIOExecutor");
                }else {
                    heavyIOExecutor = new ScheduledThreadPoolExecutor(
                            10, VirtualThreadUtil.createThreadFactory(false, "JetCacheHeavyIOExecutor-")
                    , new ThreadPoolExecutor.DiscardPolicy());
                }
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
