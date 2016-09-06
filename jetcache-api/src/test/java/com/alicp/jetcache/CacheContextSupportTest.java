/**
 * Created on  13-09-23 16:02
 */
package com.alicp.jetcache;

import com.alicp.jetcache.CacheContextSupport;
import com.alicp.jetcache.Callback;
import com.alicp.jetcache.ReturnValueCallback;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheContextSupportTest {
    @Test
    public void test(){
        final CacheContextSupport c = new CacheContextSupport();
        c.enable();
        Assert.assertTrue(c.isEnabled());
        c.disable();
        Assert.assertFalse(c.isEnabled());

        Assert.assertFalse(c.isEnabled());
        c.enableCache(new Callback() {
            @Override
            public void execute() throws Throwable {
                Assert.assertTrue(c.isEnabled());
            }
        });
        Assert.assertFalse(c.isEnabled());

        Assert.assertFalse(c.isEnabled());
        c.enableCache(new ReturnValueCallback<Object>() {
            @Override
            public Object execute() throws Throwable {
                Assert.assertTrue(c.isEnabled());
                return null;
            }
        });
        Assert.assertFalse(c.isEnabled());

        // 嵌套
        Assert.assertFalse(c.isEnabled());
        c.enableCache(new Callback() {
            @Override
            public void execute() throws Throwable {
                Assert.assertTrue(c.isEnabled());
                c.enableCache(new ReturnValueCallback<Object>() {
                    @Override
                    public Object execute() throws Throwable {
                        Assert.assertTrue(c.isEnabled());
                        c.enable();
                        c.disable();
                        return null;
                    }
                });
                Assert.assertTrue(c.isEnabled());
            }
        });
        Assert.assertFalse(c.isEnabled());
    }
}
