package com.alicp.jetcache.testsupport;

/**
 * Help class for sleep, Constructor with a base time interval in milliseconds, the start time will
 * be set to the current time.
 * <p>
 * The sleepTo method sleeps to particular time point of any multiple of interval from the start time.
 */
public class Sleeper {
    private long refresh;
    private long start;

    public Sleeper(long refresh) {
        this.refresh = refresh;
        start = System.currentTimeMillis();
    }

    public void ressetStartAt() {
        this.start = System.currentTimeMillis();
    }

    public void sleepTo(double ratio) throws InterruptedException {
        long wakeup = (long) (start + ratio * refresh);
        Thread.sleep(wakeup - System.currentTimeMillis());
    }
}
