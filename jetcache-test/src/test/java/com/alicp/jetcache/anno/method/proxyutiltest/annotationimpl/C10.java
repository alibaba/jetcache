package com.alicp.jetcache.anno.method.proxyutiltest.annotationimpl;

import com.alicp.jetcache.anno.method.interfaces.I10;

import java.util.concurrent.atomic.AtomicInteger;

public class C10 implements I10 {
    AtomicInteger count1 = new AtomicInteger(0);
    AtomicInteger count2 = new AtomicInteger(0);

    @Override
    public int count1(int p) {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return count1.incrementAndGet();
    }

    @Override
    public int count2(int p) {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return count2.incrementAndGet();
    }
}
