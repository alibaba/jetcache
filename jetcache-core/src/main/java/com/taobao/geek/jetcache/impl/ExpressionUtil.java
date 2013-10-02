/**
 * Created on  13-10-02 18:38
 */
package com.taobao.geek.jetcache.impl;

import com.taobao.geek.jetcache.CacheConfig;
import com.taobao.geek.jetcache.CacheConfigException;
import com.taobao.geek.jetcache.CacheMonitor;
import org.mvel2.MVEL;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
class ExpressionUtil {

    public static boolean evalCondition(CacheInvokeContext context) {
        String condition = context.cacheInvokeConfig.cacheConfig.getCondition();
        if (CacheConfig.DEFAULT_CONDITION.equals(condition)) {
            return true;
        }
        try {
            return (Boolean) MVEL.eval(condition, context);
        } catch (Exception e) {
            CacheMonitor cm = context.cacheProviderFactory.getCacheMonitor();
            if (cm != null) {
                cm.error("error occurs when eval condition \"" + condition + "\" in " + context.getMethod() + "." + e.getClass() + ":" + e.getMessage());
            }
            return false;
        }
    }

    public static boolean evalUnless(CacheInvokeContext context) {
        String unless = context.cacheInvokeConfig.cacheConfig.getUnless();
        if (CacheConfig.DEFAULT_UNLESS.equals(unless)) {
            return true;
        }
        try {
            return !(Boolean) MVEL.eval(unless, context);
        } catch (Exception e) {
            CacheMonitor cm = context.cacheProviderFactory.getCacheMonitor();
            if (cm != null) {
                cm.error("error occurs when eval unless \"" + unless + "\" in " + context.getMethod() + "." + e.getClass() + ":" + e.getMessage());
            }
            return false;
        }
    }


    private static final Pattern pattern = Pattern.compile("(\\w+)\\{.+\\}");

    public static EL parseEL(String script) {
        if (script == null || script.trim().equals("")) {
            return null;
        }
        Matcher matcher = pattern.matcher(script);
        String s = matcher.group(1);
        if ("spel".equals(s)) {
            return EL.SPRING_EL;
        } else if ("mvel".equals(s)) {
            return EL.MVEL;
        } else if ("buildin".equals(s)) {
            return EL.BUILD_IN;
        } else {
            throw new CacheConfigException("Can't parse \"" + script + "\"");
        }
    }
}
