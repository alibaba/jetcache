package com.alicp.jetcache.embedded;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheResultCode;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Created on 2017/3/1.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class CleanerTest {

    @Test
    public void test() throws Exception {
        Cleaner.linkedHashMapCaches.clear();
        Cache c1 = LinkedHashMapCacheBuilder.createLinkedHashMapCacheBuilder()
                .expireAfterWrite(2000, TimeUnit.MILLISECONDS).limit(3).buildCache();
        Cache c2 = LinkedHashMapCacheBuilder.createLinkedHashMapCacheBuilder()
                .expireAfterWrite(2000, TimeUnit.MILLISECONDS).limit(3).buildCache();
        c1.put("K1", "V1", 1, TimeUnit.MILLISECONDS);
        c2.put("K1", "V1", 1, TimeUnit.MILLISECONDS);
        Thread.sleep(1);
        Assert.assertEquals(CacheResultCode.EXPIRED, c1.GET("K1").getResultCode());
        Assert.assertEquals(CacheResultCode.EXPIRED, c1.GET("K1").getResultCode());
        Cleaner.run();
        Assert.assertEquals(CacheResultCode.NOT_EXISTS, c1.GET("K1").getResultCode());
        Assert.assertEquals(CacheResultCode.NOT_EXISTS, c1.GET("K1").getResultCode());

        Assert.assertEquals(2, Cleaner.linkedHashMapCaches.size());
        c1 = null;
        System.gc();
        Cleaner.run();
        Assert.assertEquals(1, Cleaner.linkedHashMapCaches.size());

    }
}
