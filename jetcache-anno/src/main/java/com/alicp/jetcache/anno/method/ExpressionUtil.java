/**
 * Created on  13-10-02 18:38
 */
package com.alicp.jetcache.anno.method;

import com.alicp.jetcache.CacheConfigException;
import com.alicp.jetcache.CacheException;
import com.alicp.jetcache.anno.CacheConsts;
import org.mvel2.MVEL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
class ExpressionUtil {

    private static final Logger logger = LoggerFactory.getLogger(ExpressionUtil.class);

    private static boolean eval(String text, CacheInvokeContext context, EL el) {
        if (el == EL.MVEL) {
            return (Boolean) MVEL.eval(text, context);
        } else {
            throw new CacheException("not support yet:" + el);
        }
    }

    public static boolean evalCondition(CacheInvokeContext context) {
        CacheInvokeConfig cic = context.getCacheInvokeConfig();
        String condition = cic.getCacheAnnoConfig().getCondition();
        if (CacheConsts.UNDEFINED_STRING.equals(condition)) {
            return true;
        }
        try {
            String script = cic.getConditionScript();
            EL el = cic.getConditionEL();
            return eval(script, context, el);
        } catch (Exception e) {
            logger.error("error occurs when eval condition \"" + condition + "\" in " + context.getMethod() + "." + e.getClass() + ":" + e.getMessage());
            return false;
        }
    }

    public static boolean evalUnless(CacheInvokeContext context) {
        CacheInvokeConfig cic = context.getCacheInvokeConfig();
        String unless = cic.getCacheAnnoConfig().getUnless();
        if (CacheConsts.UNDEFINED_STRING.equals(unless)) {
            return false;
        }
        try {
            String script = cic.getUnlessScript();
            EL el = cic.getUnlessEL();
            return eval(script, context, el);
        } catch (Exception e) {
            logger.error("error occurs when eval unless \"" + unless + "\" in " + context.getMethod() + "." + e.getClass() + ":" + e.getMessage());
            return true;
        }
    }


    private static final Pattern pattern = Pattern.compile("\\s*(\\w+)\\s*\\{(.+)\\}\\s*");

    public static Object[] parseEL(String script) {
        if (script == null || script.trim().equals("")) {
            return null;
        }
        Object[] rt = new Object[2];
        Matcher matcher = pattern.matcher(script);
        if (!matcher.matches()) {
            return null;
        }
        String s = matcher.group(1);
        if ("spel".equals(s)) {
            rt[0] = EL.SPRING_EL;
        } else if ("mvel".equals(s)) {
            rt[0] = EL.MVEL;
        } else if ("buildin".equals(s)) {
            rt[0] = EL.BUILD_IN;
        } else {
            throw new CacheConfigException("Can't parse \"" + script + "\"");
        }
        rt[1] = matcher.group(2);
        return rt;
    }
}
