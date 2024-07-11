package com.alicp.jetcache;

import java.util.concurrent.TimeUnit;

/**
 * Created on 2017/5/25.
 *
 * @author huangli
 */
public class RefreshPolicy implements Cloneable {

    private long refreshMillis;
    private long refreshIntervalMillis;
    private long refreshLockTimeoutMillis = 60 * 1000;

    public RefreshPolicy() {
    }

    public static RefreshPolicy newPolicy(long time, TimeUnit timeUnit) {
        RefreshPolicy p = new RefreshPolicy();
        p.refreshMillis = timeUnit.toMillis(time);
        return p;
    }

    public RefreshPolicy refreshIntervalMillis(long time, TimeUnit unit) {
        this.refreshIntervalMillis = unit.toMillis(time);
        return this;
    }

    public RefreshPolicy refreshLockTimeout(long time, TimeUnit timeUnit) {
        this.refreshLockTimeoutMillis = timeUnit.toMillis(time);
        return this;
    }

    @Override
    public RefreshPolicy clone() {
        try {
            return (RefreshPolicy) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public long getRefreshMillis() {
        return refreshMillis;
    }

    public void setRefreshMillis(long refreshMillis) {
        this.refreshMillis = refreshMillis;
    }

    public long getRefreshIntervalMillis() {
        return refreshIntervalMillis;
    }

    public void setRefreshIntervalMillis(long refreshIntervalMillis) {
        this.refreshIntervalMillis = refreshIntervalMillis;
    }

    public long getRefreshLockTimeoutMillis() {
        return refreshLockTimeoutMillis;
    }

    public void setRefreshLockTimeoutMillis(long refreshLockTimeoutMillis) {
        this.refreshLockTimeoutMillis = refreshLockTimeoutMillis;
    }
}
