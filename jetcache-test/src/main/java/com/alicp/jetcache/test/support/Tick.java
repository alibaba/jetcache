package com.alicp.jetcache.test.support;

/**
 * @author huangli
 */
public class Tick {
    private static final int TICK = Integer.parseInt(System.getProperty("tick", "1"));

    public static int tick(int value) {
        return value * TICK;
    }

    public static long tick(long value) {
        return value * TICK;
    }
}
