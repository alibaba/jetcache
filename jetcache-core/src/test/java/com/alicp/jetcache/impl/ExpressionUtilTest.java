/**
 * Created on  13-10-02 22:14
 */
package com.alicp.jetcache.impl;

import com.alicp.jetcache.support.CacheConfig;
import com.alicp.jetcache.support.GlobalCacheConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class ExpressionUtilTest {
    private CacheInvokeContext context;
    private CacheConfig cacheConfig;

    @Before
    public void setup() {
        context = new CacheInvokeContext();
        cacheConfig = new CacheConfig();
        context.cacheInvokeConfig = new CacheInvokeConfig();
        context.cacheInvokeConfig.cacheConfig = cacheConfig;

        context.globalCacheConfig = new GlobalCacheConfig(new HashMap());
    }

    @Test
    public void testCondition() {
        cacheConfig.setCondition("mvel{args[0]==null}");
        context.cacheInvokeConfig.init();
        Assert.assertFalse(ExpressionUtil.evalCondition(context));
        context.args = new Object[1];
        Assert.assertTrue(ExpressionUtil.evalCondition(context));
        context.args = new Object[]{"1234"};
        cacheConfig.setCondition("mvel{args[0].length()==4}");
        context.cacheInvokeConfig.init();
        Assert.assertTrue(ExpressionUtil.evalCondition(context));
    }

    @Test
    public void testUnless() {
        cacheConfig.setUnless("mvel{result==null}");
        context.cacheInvokeConfig.init();
        Assert.assertFalse(ExpressionUtil.evalUnless(context));
    }

}
