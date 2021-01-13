/**
 * Created on 2018/1/23.
 */
package com.alicp.jetcache.anno.support;

import java.util.function.Function;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class CacheUpdateAnnoConfig extends CacheAnnoConfig {

    private String value;
    private boolean multi;

    private Function<Object, Object> valueEvaluator;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Function<Object, Object> getValueEvaluator() {
        return valueEvaluator;
    }

    public void setValueEvaluator(Function<Object, Object> valueEvaluator) {
        this.valueEvaluator = valueEvaluator;
    }

    public boolean isMulti() {
        return multi;
    }

    public void setMulti(boolean multi) {
        this.multi = multi;
    }
}
