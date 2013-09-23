/**
 * Created on  13-09-23 16:02
 */
package com.taobao.geek.jetcache.impl;

import com.taobao.geek.jetcache.Callback;
import com.taobao.geek.jetcache.ReturnValueCallback;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author yeli.hl
 */
public class CacheContextSupportTest {
    @Test
    public void test(){
        CacheContextSupport.enable();
        Assert.assertTrue(CacheContextSupport.isEnabled());
        CacheContextSupport.disable();
        Assert.assertFalse(CacheContextSupport.isEnabled());

        Assert.assertFalse(CacheContextSupport.isEnabled());
        CacheContextSupport.enableCache(new Callback() {
            @Override
            public void execute() throws Throwable {
                Assert.assertTrue(CacheContextSupport.isEnabled());
            }
        });
        Assert.assertFalse(CacheContextSupport.isEnabled());

        Assert.assertFalse(CacheContextSupport.isEnabled());
        CacheContextSupport.enableCache(new ReturnValueCallback<Object>() {
            @Override
            public Object execute() throws Throwable {
                Assert.assertTrue(CacheContextSupport.isEnabled());
                return null;
            }
        });
        Assert.assertFalse(CacheContextSupport.isEnabled());

        // 嵌套
        Assert.assertFalse(CacheContextSupport.isEnabled());
        CacheContextSupport.enableCache(new Callback() {
            @Override
            public void execute() throws Throwable {
                Assert.assertTrue(CacheContextSupport.isEnabled());
                CacheContextSupport.enableCache(new ReturnValueCallback<Object>() {
                    @Override
                    public Object execute() throws Throwable {
                        Assert.assertTrue(CacheContextSupport.isEnabled());
                        CacheContextSupport.enable();
                        CacheContextSupport.disable();
                        return null;
                    }
                });
                Assert.assertTrue(CacheContextSupport.isEnabled());
            }
        });
        Assert.assertFalse(CacheContextSupport.isEnabled());
    }
}
