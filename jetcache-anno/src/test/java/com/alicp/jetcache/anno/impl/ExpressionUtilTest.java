/**
 * Created on  13-10-02 22:14
 */
package com.alicp.jetcache.anno.impl;

import com.alicp.jetcache.anno.support.CacheAnnoConfig;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class ExpressionUtilTest {
    private CacheInvokeContext context;
    private CacheAnnoConfig cacheAnnoConfig;

    @Before
    public void setup() {
        context = new CacheInvokeContext();
        cacheAnnoConfig = new CacheAnnoConfig();
        context.cacheInvokeConfig = new CacheInvokeConfig();
        context.cacheInvokeConfig.cacheAnnoConfig = cacheAnnoConfig;

        context.globalCacheConfig = new GlobalCacheConfig();
    }

    @Test
    public void testCondition() {
        cacheAnnoConfig.setCondition("mvel{args[0]==null}");
        context.cacheInvokeConfig.init();
        Assert.assertFalse(ExpressionUtil.evalCondition(context));
        context.args = new Object[1];
        Assert.assertTrue(ExpressionUtil.evalCondition(context));
        context.args = new Object[]{"1234"};
        cacheAnnoConfig.setCondition("mvel{args[0].length()==4}");
        context.cacheInvokeConfig.init();
        Assert.assertTrue(ExpressionUtil.evalCondition(context));
    }

    @Test
    public void testUnless() {
        cacheAnnoConfig.setUnless("mvel{result==null}");
        context.cacheInvokeConfig.init();
        Assert.assertFalse(ExpressionUtil.evalUnless(context));
    }

}
