package com.alicp.jetcache.support;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created on 2024/08/13.
 *
 * @author huangli
 */
public class Epoch {
    private static final AtomicLong V = new AtomicLong();

    public static long increment() {
        return V.incrementAndGet();
    }

    public static long get() {
        return V.get();
    }
}
