package jetcache.samples.springboot;

import com.alicp.jetcache.support.CacheMessage;

public class CacheMessageWithName {

    private String area;

    private String cacheName;

    private CacheMessage cacheMessage;

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getCacheName() {
        return cacheName;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    public CacheMessage getCacheMessage() {
        return cacheMessage;
    }

    public void setCacheMessage(CacheMessage cacheMessage) {
        this.cacheMessage = cacheMessage;
    }

}
