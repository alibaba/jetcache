/**
 * Created on  13-09-23 16:02
 */
package com.alicp.jetcache.anno.support;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
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
