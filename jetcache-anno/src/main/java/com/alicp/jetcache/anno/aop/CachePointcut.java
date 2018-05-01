/**
 * Created on  13-09-19 20:56
 */
package com.alicp.jetcache.anno.aop;

import com.alicp.jetcache.anno.method.CacheConfigUtil;
import com.alicp.jetcache.anno.method.CacheInvokeConfig;
import com.alicp.jetcache.anno.method.ClassUtil;
import com.alicp.jetcache.anno.support.ConfigMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.asm.Type;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class CachePointcut extends StaticMethodMatcherPointcut implements ClassFilter {

    private static final Logger logger = LoggerFactory.getLogger(CachePointcut.class);

    private ConfigMap cacheConfigMap;
    private String[] basePackages;

    public CachePointcut(String[] basePackages) {
        setClassFilter(this);
        this.basePackages = basePackages;
    }

    public boolean matches(Class clazz) {
        boolean b = matchesImpl(clazz);
        logger.trace("check class match {}: {}", b, clazz);
        return b;
    }

    private boolean matchesImpl(Class clazz) {
        if (matchesThis(clazz)) {
            return true;
        }
        Class[] cs = clazz.getInterfaces();
        if (cs != null) {
            for (Class c : cs) {
                if (matchesImpl(c)) {
                    return true;
                }
            }
        }
        if (!clazz.isInterface()) {
            Class sp = clazz.getSuperclass();
            if (sp != null && matchesImpl(sp)) {
                return true;
            }
        }
        return false;
    }

    public boolean matchesThis(Class clazz) {
        String name = clazz.getName();
        if (exclude(name)) {
            return false;
        }
        return include(name);
    }

    private boolean include(String name) {
        if (basePackages != null) {
            for (String p : basePackages) {
                if (name.startsWith(p)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean exclude(String name) {
        if (name.startsWith("java")) {
            return true;
        }
        if (name.startsWith("org.springframework")) {
            return true;
        }
        if (name.indexOf("$$EnhancerBySpringCGLIB$$") >= 0) {
            return true;
        }
        return false;
    }

    public boolean matches(Method method, Class targetClass) {
        boolean b = matchesImpl(method, targetClass);
        if (b) {
            if (logger.isDebugEnabled()) {
                logger.debug("check method match true: method={}, declaringClass={}, targetClass={}",
                        method.getName(),
                        ClassUtil.getShortClassName(method.getDeclaringClass().getName()),
                        targetClass == null ? null : ClassUtil.getShortClassName(targetClass.getName()));
            }
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("check method match false: method={}, declaringClass={}, targetClass={}",
                        method.getName(),
                        ClassUtil.getShortClassName(method.getDeclaringClass().getName()),
                        targetClass == null ? null : ClassUtil.getShortClassName(targetClass.getName()));
            }
        }
        return b;
    }

    private boolean matchesImpl(Method method, Class targetClass) {
        if (!matchesThis(method.getDeclaringClass())) {
            return false;
        }
        String key = getKey(method, targetClass);
        CacheInvokeConfig cac = cacheConfigMap.getByMethodInfo(key);
        if (cac == CacheInvokeConfig.getNoCacheInvokeConfigInstance()) {
            return false;
        } else if (cac != null) {
            return true;
        } else {
            cac = new CacheInvokeConfig();
            CacheConfigUtil.parse(cac, method);

            String name = method.getName();
            Class<?>[] paramTypes = method.getParameterTypes();
            parseByTargetClass(cac, targetClass, name, paramTypes);

            if (!cac.isEnableCacheContext() && cac.getCachedAnnoConfig() == null &&
                    cac.getInvalidateAnnoConfig() == null && cac.getUpdateAnnoConfig() == null) {
                cacheConfigMap.putByMethodInfo(key, CacheInvokeConfig.getNoCacheInvokeConfigInstance());
                return false;
            } else {
                cacheConfigMap.putByMethodInfo(key, cac);
                return true;
            }
        }
    }

    public static String getKey(Method method, Class targetClass) {
        StringBuilder sb = new StringBuilder();
        sb.append(method.getDeclaringClass().getName());
        sb.append('.');
        sb.append(method.getName());
        sb.append(Type.getMethodDescriptor(method));
        if (targetClass != null) {
            sb.append('_');
            sb.append(targetClass.getName());
        }
        return sb.toString();
    }

    private void parseByTargetClass(CacheInvokeConfig cac, Class<?> clazz, String name, Class<?>[] paramTypes) {
        if (!clazz.isInterface() && clazz.getSuperclass() != null) {
            parseByTargetClass(cac, clazz.getSuperclass(), name, paramTypes);
        }
        Class<?>[] intfs = clazz.getInterfaces();
        for (Class<?> it : intfs) {
            parseByTargetClass(cac, it, name, paramTypes);
        }

        boolean matchThis = matchesThis(clazz);
        if (matchThis) {
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                if (methodMatch(name, method, paramTypes)) {
                    CacheConfigUtil.parse(cac, method);
                    break;
                }
            }
        }
    }

    private boolean methodMatch(String name, Method method, Class<?>[] paramTypes) {
        if (!Modifier.isPublic(method.getModifiers())) {
            return false;
        }
        if (!name.equals(method.getName())) {
            return false;
        }
        Class<?>[] ps = method.getParameterTypes();
        if (ps.length != paramTypes.length) {
            return false;
        }
        for (int i = 0; i < ps.length; i++) {
            if (!ps[i].equals(paramTypes[i])) {
                return false;
            }
        }
        return true;
    }


    public void setCacheConfigMap(ConfigMap cacheConfigMap) {
        this.cacheConfigMap = cacheConfigMap;
    }
}
