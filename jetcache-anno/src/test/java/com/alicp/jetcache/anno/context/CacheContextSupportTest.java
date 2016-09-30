/**
 * Created on  13-09-23 16:02
 */
package com.alicp.jetcache.anno.context;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheContextSupportTest {
    @Test
    public void test(){
        CacheContext.enable();
        Assert.assertTrue(CacheContext.isEnabled());
        CacheContext.disable();
        Assert.assertFalse(CacheContext.isEnabled());

        Assert.assertFalse(CacheContext.isEnabled());
        CacheContext.enableCache(new Callback() {
            public void execute() throws Throwable {
                Assert.assertTrue(CacheContext.isEnabled());
            }
        });
        Assert.assertFalse(CacheContext.isEnabled());

        Assert.assertFalse(CacheContext.isEnabled());
        CacheContext.enableCache(new ReturnValueCallback<Object>() {
            public Object execute() throws Throwable {
                Assert.assertTrue(CacheContext.isEnabled());
                return null;
            }
        });
        Assert.assertFalse(CacheContext.isEnabled());

        // 嵌套
        Assert.assertFalse(CacheContext.isEnabled());
        CacheContext.enableCache(new Callback() {
            public void execute() throws Throwable {
                Assert.assertTrue(CacheContext.isEnabled());
                CacheContext.enableCache(new ReturnValueCallback<Object>() {
                    public Object execute() throws Throwable {
                        Assert.assertTrue(CacheContext.isEnabled());
                        CacheContext.enable();
                        CacheContext.disable();
                        return null;
                    }
                });
                Assert.assertTrue(CacheContext.isEnabled());
            }
        });
        Assert.assertFalse(CacheContext.isEnabled());
    }
}
