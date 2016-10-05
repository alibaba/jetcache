/**
 * Created on  13-10-02 18:38
 */
package com.alicp.jetcache.anno.impl;

import com.alicp.jetcache.*;
import com.alicp.jetcache.anno.CacheConsts;
import com.alicp.jetcache.anno.support.CacheMonitor;
import org.mvel2.MVEL;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
class ExpressionUtil {

    private static boolean eval(String text, CacheInvokeContext context, EL el) {
        if (el == EL.MVEL) {
            return (Boolean) MVEL.eval(text, context);
        } else {
            throw new CacheException("not support yet:" + el);
        }
    }

    public static boolean evalCondition(CacheInvokeContext context) {
        String condition = context.cacheInvokeConfig.cacheAnnoConfig.getCondition();
        if (CacheConsts.DEFAULT_CONDITION.equals(condition)) {
            return true;
        }
        try {
            return eval(context.cacheInvokeConfig.conditionScript, context, context.cacheInvokeConfig.conditionEL);
        } catch (Exception e) {
            CacheMonitor cm = context.globalCacheConfig.getCacheMonitor();
            if (cm != null) {
                cm.error("error occurs when eval condition \"" + condition + "\" in " + context.getMethod() + "." + e.getClass() + ":" + e.getMessage());
            }
            return false;
        }
    }

    public static boolean evalUnless(CacheInvokeContext context) {
        String unless = context.cacheInvokeConfig.cacheAnnoConfig.getUnless();
        if (CacheConsts.DEFAULT_UNLESS.equals(unless)) {
            return true;
        }
        try {
            return !eval(context.cacheInvokeConfig.unlessScript, context, context.cacheInvokeConfig.unlessEL);
        } catch (Exception e) {
            CacheMonitor cm = context.globalCacheConfig.getCacheMonitor();
            if (cm != null) {
                cm.error("error occurs when eval unless \"" + unless + "\" in " + context.getMethod() + "." + e.getClass() + ":" + e.getMessage());
            }
            return false;
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
