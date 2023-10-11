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
 * @author huangli
 */
public class CacheContextTest {
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
