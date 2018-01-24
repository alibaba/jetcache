/**
 * Created on  13-10-02 22:14
 */
package com.alicp.jetcache.anno.method;

import com.alicp.jetcache.anno.CacheConsts;
import com.alicp.jetcache.anno.support.CachedAnnoConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class ExpressionUtilTest {
    private CacheInvokeContext context;
    private CachedAnnoConfig cachedAnnoConfig;
    private CacheInvokeConfig cic;

    public void targetMethod(String p1, int p2) {
    }

    @Before
    public void setup() throws Exception {
        context = new CacheInvokeContext();
        cachedAnnoConfig = new CachedAnnoConfig();
        cachedAnnoConfig.setDefineMethod(ExpressionUtilTest.class.getMethod("targetMethod", String.class, int.class));
        cic = new CacheInvokeConfig();
        context.setCacheInvokeConfig(cic);
        context.getCacheInvokeConfig().setCachedAnnoConfig(cachedAnnoConfig);
    }

    @Test
    public void testCondition1() {
        cachedAnnoConfig.setCondition("mvel{args[0]==null}");
        Assert.assertFalse(ExpressionUtil.evalCondition(context, cachedAnnoConfig));
        context.setArgs(new Object[2]);
        Assert.assertTrue(ExpressionUtil.evalCondition(context, cachedAnnoConfig));
    }
    @Test
    public void testCondition2() {
        cachedAnnoConfig.setCondition("mvel{args[0].length()==4}");
        context.setArgs(new Object[]{"1234", 5678});
        Assert.assertTrue(ExpressionUtil.evalCondition(context, cachedAnnoConfig));
    }

    @Test
    public void testCondition3() {
        cachedAnnoConfig.setCondition(CacheConsts.UNDEFINED_STRING);
        Assert.assertTrue(ExpressionUtil.evalCondition(context, cachedAnnoConfig));
    }

    @Test
    public void testUnless1() {
        cachedAnnoConfig.setUnless("result==null");
        context.setArgs(new Object[]{"1234", 5678});
        Assert.assertTrue(ExpressionUtil.evalUnless(context));
    }

    @Test
    public void testUnless2() {
        cachedAnnoConfig.setUnless("#p2==1000");
        context.setArgs(new Object[]{"1234", 5678});
        Assert.assertFalse(ExpressionUtil.evalUnless(context));
    }

    @Test
    public void testUnless3() {
        cachedAnnoConfig.setUnless(CacheConsts.UNDEFINED_STRING);
        context.setArgs(new Object[]{"1234", 5678});
        Assert.assertFalse(ExpressionUtil.evalUnless(context));
    }
}
