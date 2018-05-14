/**
 * Created on  13-10-02 18:38
 */
package com.alicp.jetcache.anno.method;

import com.alicp.jetcache.anno.CacheConsts;
import com.alicp.jetcache.anno.support.CacheAnnoConfig;
import com.alicp.jetcache.anno.support.CacheUpdateAnnoConfig;
import com.alicp.jetcache.anno.support.CachedAnnoConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
class ExpressionUtil {

    private static final Logger logger = LoggerFactory.getLogger(ExpressionUtil.class);

    static Object EVAL_FAILED = new Object();

    public static boolean evalCondition(CacheInvokeContext context, CacheAnnoConfig cac) {
        String condition = cac.getCondition();
        try {
            if (cac.getConditionEvaluator() == null) {
                if (CacheConsts.isUndefined(condition)) {
                    cac.setConditionEvaluator(o -> true);
                } else {
                    ExpressionEvaluator e = new ExpressionEvaluator(condition, cac.getDefineMethod());
                    cac.setConditionEvaluator((o) -> (Boolean) e.apply(o));
                }
            }
            return cac.getConditionEvaluator().apply(context);
        } catch (Exception e) {
            logger.error("error occurs when eval condition \"" + condition + "\" in " + context.getMethod() + ":" + e.getMessage(), e);
            return false;
        }
    }

    public static boolean evalPostCondition(CacheInvokeContext context, CachedAnnoConfig cac) {
        String postCondition = cac.getPostCondition();
        try {
            if (cac.getPostConditionEvaluator() == null) {
                if (CacheConsts.isUndefined(postCondition)) {
                    cac.setPostConditionEvaluator(o -> true);
                } else {
                    ExpressionEvaluator e = new ExpressionEvaluator(postCondition, cac.getDefineMethod());
                    cac.setPostConditionEvaluator((o) -> (Boolean) e.apply(o));
                }
            }
            return cac.getPostConditionEvaluator().apply(context);
        } catch (Exception e) {
            logger.error("error occurs when eval postCondition \"" + postCondition + "\" in " + context.getMethod() + ":" + e.getMessage(), e);
            return false;
        }
    }

    public static Object evalKey(CacheInvokeContext context, CacheAnnoConfig cac) {
        String keyScript = cac.getKey();
        try {
            if (cac.getKeyEvaluator() == null) {
                if (CacheConsts.isUndefined(keyScript)) {
                    cac.setKeyEvaluator(o -> {
                        CacheInvokeContext c = (CacheInvokeContext) o;
                        return c.getArgs() == null ? "_$JETCACHE_NULL_KEY$_" : c.getArgs();
                    });
                } else {
                    ExpressionEvaluator e = new ExpressionEvaluator(keyScript, cac.getDefineMethod());
                    cac.setKeyEvaluator((o) -> e.apply(o));
                }
            }
            return cac.getKeyEvaluator().apply(context);
        } catch (Exception e) {
            logger.error("error occurs when eval key \"" + keyScript + "\" in " + context.getMethod() + ":" + e.getMessage(), e);
            return null;
        }
    }

    public static Object evalValue(CacheInvokeContext context, CacheUpdateAnnoConfig cac) {
        String valueScript = cac.getValue();
        try {
            if (cac.getValueEvaluator() == null) {
                ExpressionEvaluator e = new ExpressionEvaluator(valueScript, cac.getDefineMethod());
                cac.setValueEvaluator((o) -> e.apply(o));
            }
            return cac.getValueEvaluator().apply(context);
        } catch (Exception e) {
            logger.error("error occurs when eval value \"" + valueScript + "\" in " + context.getMethod() + ":" + e.getMessage(), e);
            return EVAL_FAILED;
        }
    }
}
