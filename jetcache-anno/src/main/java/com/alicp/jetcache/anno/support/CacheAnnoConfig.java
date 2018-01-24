/**
 * Created on 2018/1/22.
 */
package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.Cache;

import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class CacheAnnoConfig {
    private String area;
    private String name;
    private String key;
    private String condition;

    private Function<Object, Boolean> conditionEvaluator;
    private Function<Object, Object> keyEvaluator;
    private Cache<?, ?> cache;
    private Method defineMethod;

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public Function<Object, Boolean> getConditionEvaluator() {
        return conditionEvaluator;
    }

    public void setConditionEvaluator(Function<Object, Boolean> conditionEvaluator) {
        this.conditionEvaluator = conditionEvaluator;
    }

    public Function<Object, Object> getKeyEvaluator() {
        return keyEvaluator;
    }

    public void setKeyEvaluator(Function<Object, Object> keyEvaluator) {
        this.keyEvaluator = keyEvaluator;
    }

    public Cache<?, ?> getCache() {
        return cache;
    }

    public void setCache(Cache<?, ?> cache) {
        this.cache = cache;
    }

    public Method getDefineMethod() {
        return defineMethod;
    }

    public void setDefineMethod(Method defineMethod) {
        this.defineMethod = defineMethod;
    }
}
