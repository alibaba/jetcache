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

    @Before
    public void setup() {
        context = new CacheInvokeContext();
        cachedAnnoConfig = new CachedAnnoConfig();
        cic = new CacheInvokeConfig();
        context.setCacheInvokeConfig(cic);
        context.getCacheInvokeConfig().setCachedAnnoConfig(cachedAnnoConfig);
    }

    @Test
    public void testCondition1() {
        cachedAnnoConfig.setCondition("mvel{args[0]==null}");
        Assert.assertFalse(ExpressionUtil.evalCondition(context, cachedAnnoConfig.getCondition(),
                cic::getCachedConditionEvaluator, cic::setCachedConditionEvaluator));
        context.setArgs(new Object[1]);
        Assert.assertTrue(ExpressionUtil.evalCondition(context, cachedAnnoConfig.getCondition(),
                cic::getCachedConditionEvaluator, cic::setCachedConditionEvaluator));
    }
    @Test
    public void testCondition2() {
        cachedAnnoConfig.setCondition("mvel{args[0].length()==4}");
        context.setArgs(new Object[]{"1234"});
        Assert.assertTrue(ExpressionUtil.evalCondition(context, cachedAnnoConfig.getCondition(),
                cic::getCachedConditionEvaluator, cic::setCachedConditionEvaluator));
    }

    @Test
    public void testCondition3() {
        cachedAnnoConfig.setCondition(CacheConsts.UNDEFINED_STRING);
        Assert.assertTrue(ExpressionUtil.evalCondition(context, cachedAnnoConfig.getCondition(),
                cic::getCachedConditionEvaluator, cic::setCachedConditionEvaluator));
    }

    @Test
    public void testUnless1() {
        cachedAnnoConfig.setUnless("result==null");
        Assert.assertTrue(ExpressionUtil.evalUnless(context));
    }

    @Test
    public void testUnless2() {
        cachedAnnoConfig.setUnless("result!=null");
        Assert.assertFalse(ExpressionUtil.evalUnless(context));
    }

    @Test
    public void testUnless3() {
        cachedAnnoConfig.setUnless(CacheConsts.UNDEFINED_STRING);
        Assert.assertFalse(ExpressionUtil.evalUnless(context));
    }
}
