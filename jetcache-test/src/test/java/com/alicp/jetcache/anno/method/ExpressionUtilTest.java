/**
 * Created on  13-10-02 22:14
 */
package com.alicp.jetcache.anno.method;

import com.alicp.jetcache.anno.CacheConsts;
import com.alicp.jetcache.anno.support.CacheAnnoConfig;
import com.alicp.jetcache.anno.support.CacheContext;
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
    public void testCondition1() {
        cacheAnnoConfig.setCondition("mvel{args[0]==null}");
        Assert.assertFalse(ExpressionUtil.evalCondition(context));
        context.setArgs(new Object[1]);
        Assert.assertTrue(ExpressionUtil.evalCondition(context));
    }
    @Test
    public void testCondition2() {
        cacheAnnoConfig.setCondition("mvel{args[0].length()==4}");
        context.setArgs(new Object[]{"1234"});
        Assert.assertTrue(ExpressionUtil.evalCondition(context));
    }

    @Test
    public void testCondition3() {
        cacheAnnoConfig.setCondition(CacheConsts.UNDEFINED_STRING);
        Assert.assertTrue(ExpressionUtil.evalCondition(context));
    }

    @Test
    public void testUnless1() {
        cacheAnnoConfig.setUnless("result==null");
        Assert.assertTrue(ExpressionUtil.evalUnless(context));
    }

    @Test
    public void testUnless2() {
        cacheAnnoConfig.setUnless("result!=null");
        Assert.assertFalse(ExpressionUtil.evalUnless(context));
    }

    @Test
    public void testUnless3() {
        cacheAnnoConfig.setUnless(CacheConsts.UNDEFINED_STRING);
        Assert.assertFalse(ExpressionUtil.evalUnless(context));
    }
}
