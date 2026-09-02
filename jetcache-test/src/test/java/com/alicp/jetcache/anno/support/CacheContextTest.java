/**
 * Created on  13-09-23 16:02
 */
package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.VirtualThreadUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
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
        Assertions.assertTrue(CacheContext.isEnabled());
        CacheContext.disable();
        Assertions.assertFalse(CacheContext.isEnabled());

        Assertions.assertFalse(CacheContext.isEnabled());
        CacheContext.enableCache(() -> {
            Assertions.assertTrue(CacheContext.isEnabled());
            return null;
        });
        Assertions.assertFalse(CacheContext.isEnabled());

        Assertions.assertFalse(CacheContext.isEnabled());
        CacheContext.enableCache(() -> {
            Assertions.assertTrue(CacheContext.isEnabled());
            CacheContext.enableCache(() -> {
                Assertions.assertTrue(CacheContext.isEnabled());
                CacheContext.enable();
                CacheContext.disable();
                return null;
            });
            Assertions.assertTrue(CacheContext.isEnabled());
            return null;
        });
        Assertions.assertFalse(CacheContext.isEnabled());
    }
}
