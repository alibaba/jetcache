/**
 * Created on  13-10-02 22:14
 */
package com.taobao.geek.jetcache.impl;

import com.taobao.geek.jetcache.CacheConfig;
import com.taobao.geek.jetcache.CacheProviderFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class ExpressionUtilTest {
    private CacheInvokeContext context;

    @Before
    public void setup() {
        context = new CacheInvokeContext();
        context.cacheConfig = new CacheConfig();
        context.cacheProviderFactory = new CacheProviderFactory(new HashMap());
    }

    @Test
    public void testCondition() {
        context.cacheConfig.setCondition("args[0]==null");
        Assert.assertFalse(ExpressionUtil.evalCondition(context));
        context.args = new Object[1];
        Assert.assertTrue(ExpressionUtil.evalCondition(context));
        context.args = new Object[]{"1234"};
        context.cacheConfig.setCondition("args[0].length()==4");
        Assert.assertTrue(ExpressionUtil.evalCondition(context));
    }

    @Test
    public void testUnless() {
        context.cacheConfig.setUnless("result==null");
        Assert.assertFalse(ExpressionUtil.evalUnless(context));
    }

}
