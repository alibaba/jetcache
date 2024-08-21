package com.alicp.jetcache.embedded;

import com.alicp.jetcache.support.JetCacheExecutor;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2017/2/28.
 *
 * @author huangli
 */
class Cleaner {

    static ConcurrentLinkedQueue<WeakReference<LinkedHashMapCache>> linkedHashMapCaches = new ConcurrentLinkedQueue<>();

    static {
        ScheduledExecutorService executorService = JetCacheExecutor.defaultExecutor();
        executorService.scheduleWithFixedDelay(() -> run(), 60, 60, TimeUnit.SECONDS);
    }

    static void add(LinkedHashMapCache cache) {
        linkedHashMapCaches.add(new WeakReference<>(cache));
    }

    static void run() {
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
    }

}
