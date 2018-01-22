/**
 * Created on  13-10-02 18:38
 */
package com.alicp.jetcache.anno.method;

import com.alicp.jetcache.anno.CacheConsts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
class ExpressionUtil {

    private static final Logger logger = LoggerFactory.getLogger(ExpressionUtil.class);

    public static boolean evalCondition(CacheInvokeContext context, String condition,
                                        Supplier<Function<Object, Boolean>> evaluatorGetter,
                                        Consumer<Function<Object, Boolean>> evaluatorSetter) {
        try {
            if (evaluatorGetter.get() == null) {
                if (CacheConsts.UNDEFINED_STRING.equals(condition)) {
                    evaluatorSetter.accept(o -> true);
                } else {
                    ExpressionEvaluator e = new ExpressionEvaluator(condition);
                    evaluatorSetter.accept((o) -> (Boolean) e.apply(o));
                }
            }
            return evaluatorGetter.get().apply(context);
        } catch (Exception e) {
            logger.error("error occurs when eval condition \"" + condition + "\" in " + context.getMethod() + "." + e.getClass() + ":" + e.getMessage());
            return false;
        }
    }

    public static boolean evalUnless(CacheInvokeContext context) {
        CacheInvokeConfig cic = context.getCacheInvokeConfig();
        String unless = cic.getCachedAnnoConfig().getUnless();
        try {
            if (cic.getCachedUnlessEvaluator() == null) {
                if (CacheConsts.UNDEFINED_STRING.equals(unless)) {
                    cic.setCachedUnlessEvaluator(o -> false);
                } else {
                    ExpressionEvaluator e = new ExpressionEvaluator(unless);
                    cic.setCachedUnlessEvaluator((o) -> (Boolean) e.apply(o));
                }
            }
            return cic.getCachedUnlessEvaluator().apply(context);
        } catch (Exception e) {
            logger.error("error occurs when eval unless \"" + unless + "\" in " + context.getMethod() + "." + e.getClass() + ":" + e.getMessage());
            return true;
        }
    }

    public static Object evalKey(CacheInvokeContext context, String keyScript,
                                 Supplier<Function<Object, Object>> evaluatorGetter,
                                 Consumer<Function<Object, Object>> evaluatorSetter) {
        try {
            if (evaluatorGetter.get() == null) {
                if (CacheConsts.UNDEFINED_STRING.equals(keyScript)) {
                    evaluatorSetter.accept(o -> {
                        CacheInvokeContext c = (CacheInvokeContext) o;
                        return c.getArgs() == null ? "_$JETCACHE_NULL_KEY$_" : c.getArgs();
                    });
                } else {
                    ExpressionEvaluator e = new ExpressionEvaluator(keyScript);
                    evaluatorSetter.accept((o) -> e.apply(o));
                }
            }
            return evaluatorGetter.get().apply(context);
        } catch (Exception e) {
            logger.error("error occurs when eval key \"" + keyScript + "\" in " + context.getMethod() + "." + e.getClass() + ":" + e.getMessage());
            return null;
        }
    }

}
