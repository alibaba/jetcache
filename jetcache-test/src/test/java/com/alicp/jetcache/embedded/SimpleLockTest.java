package com.alicp.jetcache.embedded;

import com.alicp.jetcache.Cache;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/12/21.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class SimpleLockTest {
    @Test
    public void test() throws Exception {
        Cache<Object, Object> cache = LinkedHashMapCacheBuilder.createLinkedHashMapCacheBuilder().limit(100).buildCache();
        Assert.assertNotNull(SimpleLock.tryLock(cache, "K1", 20, TimeUnit.MILLISECONDS));
        Assert.assertNull(SimpleLock.tryLock(cache, "K1", 20, TimeUnit.MILLISECONDS));
        Thread.sleep(20);
        Assert.assertNotNull(SimpleLock.tryLock(cache, "K1", 20, TimeUnit.MILLISECONDS));
        try(SimpleLock lock = SimpleLock.tryLock(cache, "K2", 20, TimeUnit.MILLISECONDS)){
            Assert.assertNotNull(lock);
            Assert.assertNull(SimpleLock.tryLock(cache, "K2", 20, TimeUnit.MILLISECONDS));
        }
        Assert.assertNotNull(SimpleLock.tryLock(cache, "K2", 20, TimeUnit.MILLISECONDS));
    }
}
