package com.alicp.jetcache.embedded;

import com.alicp.jetcache.support.JetCacheExecutor;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created on 2017/2/28.
 *
 * @author huangli
 */
class Cleaner {

    static LinkedList<WeakReference<LinkedHashMapCache>> linkedHashMapCaches = new LinkedList<>();
    private static final ReentrantLock reentrantLock = new  ReentrantLock();

    static {
        ScheduledExecutorService executorService = JetCacheExecutor.defaultExecutor();
        executorService.scheduleWithFixedDelay(() -> run(), 60, 60, TimeUnit.SECONDS);
    }

    static void add(LinkedHashMapCache cache) {
        reentrantLock.lock();
        try{
            linkedHashMapCaches.add(new WeakReference<>(cache));
        }finally {
            reentrantLock.unlock();
        }
    }

    static void run() {
        reentrantLock.lock();
        try{
            Iterator<WeakReference<LinkedHashMapCache>> it = linkedHashMapCaches.iterator();
            while (it.hasNext()) {
                WeakReference<LinkedHashMapCache> ref = it.next();
                LinkedHashMapCache c = ref.get();
                if (c == null) {
                    it.remove();
                } else {
                    c.cleanExpiredEntry();
                }
            }
        }finally {
            reentrantLock.unlock();
        }
    }

}
