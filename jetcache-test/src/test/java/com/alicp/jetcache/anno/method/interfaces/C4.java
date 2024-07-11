package com.alicp.jetcache.anno.method.interfaces;

public class C4 implements I4_2 {
    int count;

    public int count() {
        return count++;
    }

    public int countWithoutCache() {
        return count++;
    }
}