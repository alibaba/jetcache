/**
 * Created on  13-10-02 22:14
 */
package com.alicp.jetcache.anno.method;

import com.alicp.jetcache.anno.support.CacheAnnoConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class ExpressionUtilTest {
    private CacheInvokeContext context;
    private CacheAnnoConfig cacheAnnoConfig;

    @Before
    public void setup() {
        context = new CacheInvokeContext();
        cacheAnnoConfig = new CacheAnnoConfig();
        context.setCacheInvokeConfig(new CacheInvokeConfig());
        context.getCacheInvokeConfig().setCacheAnnoConfig(cacheAnnoConfig);
    }

    @Test
    public void testCondition() {
        cacheAnnoConfig.setCondition("mvel{args[0]==null}");
        context.getCacheInvokeConfig().init();
        Assert.assertFalse(ExpressionUtil.evalCondition(context));
        context.setArgs(new Object[1]);
        Assert.assertTrue(ExpressionUtil.evalCondition(context));
        context.setArgs(new Object[]{"1234"});
        cacheAnnoConfig.setCondition("mvel{args[0].length()==4}");
        context.getCacheInvokeConfig().init();
        Assert.assertTrue(ExpressionUtil.evalCondition(context));
    }

    @Test
    public void testUnless() {
        cacheAnnoConfig.setUnless("mvel{result==null}");
        context.getCacheInvokeConfig().init();
        Assert.assertTrue(ExpressionUtil.evalUnless(context));
    }

}
