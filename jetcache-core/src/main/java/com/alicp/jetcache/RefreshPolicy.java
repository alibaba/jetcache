package com.alicp.jetcache;

/**
 * Created on 2017/5/25.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class RefreshPolicy implements Cloneable {

    private long refreshMillis;
    private long stopRefreshAfterLastAccessMillis;
    private long loadLockTimeoutMillis = 60 * 1000;

    public RefreshPolicy() {
    }

    @Override
    public RefreshPolicy clone() {
        try {
            return (RefreshPolicy) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new CacheException(e);
        }
    }

    public long getRefreshMillis() {
        return refreshMillis;
    }

    public void setRefreshMillis(long refreshMillis) {
        this.refreshMillis = refreshMillis;
    }

    public long getStopRefreshAfterLastAccessMillis() {
        return stopRefreshAfterLastAccessMillis;
    }

    public void setStopRefreshAfterLastAccessMillis(long stopRefreshAfterLastAccessMillis) {
        this.stopRefreshAfterLastAccessMillis = stopRefreshAfterLastAccessMillis;
    }

    public long getLoadLockTimeoutMillis() {
        return loadLockTimeoutMillis;
    }

    public void setLoadLockTimeoutMillis(long loadLockTimeoutMillis) {
        this.loadLockTimeoutMillis = loadLockTimeoutMillis;
    }
}
