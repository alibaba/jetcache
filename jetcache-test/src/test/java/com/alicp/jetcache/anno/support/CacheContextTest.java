/**
 * Created on  13-09-23 16:02
 */
package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.VirtualThreadUtil;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.cglib.core.ReflectUtils;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class CacheContextTest {

    @Test
    public void testVirtualThreadTL() throws InterruptedException {
        ExecutorService executorService = VirtualThreadUtil.createExecuteor();
        if(executorService == null) return;
        for (int i = 0; i < 1000; i++) {
            executorService.submit(this::test);
        }
        executorService.shutdown();
        executorService.awaitTermination(3, TimeUnit.SECONDS);
    }

    @Test
    public void testFixThreadTL() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        for (int i = 0; i < 10; i++) {
            executorService.submit(this::test);
        }
        executorService.shutdown();
        executorService.awaitTermination(3, TimeUnit.SECONDS);
    }


    @Test
    public void test() {
        CacheContext.enable();
        Assert.assertTrue(CacheContext.isEnabled());
        CacheContext.disable();
        Assert.assertFalse(CacheContext.isEnabled());

        Assert.assertFalse(CacheContext.isEnabled());
        CacheContext.enableCache(() -> {
            Assert.assertTrue(CacheContext.isEnabled());
            return null;
        });
        Assert.assertFalse(CacheContext.isEnabled());

        Assert.assertFalse(CacheContext.isEnabled());
        CacheContext.enableCache(() -> {
            Assert.assertTrue(CacheContext.isEnabled());
            CacheContext.enableCache(() -> {
                Assert.assertTrue(CacheContext.isEnabled());
                CacheContext.enable();
                CacheContext.disable();
                return null;
            });
            Assert.assertTrue(CacheContext.isEnabled());
            return null;
        });
        Assert.assertFalse(CacheContext.isEnabled());
    }
}
