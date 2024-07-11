package com.alicp.jetcache.anno.method.interfaces;

public class C12 implements I12 {
    int count;

    @Override
    public int count(int p) {
        return count++;
    }
}