package com.alicp.jetcache.anno.support;

import java.util.function.Function;

/**
 * Created on 2022/04/06.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class ParserFunction<T, R> implements Function<T, R> {

    private final String value;

    public ParserFunction(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public R apply(T t) {
        throw new UnsupportedOperationException();
    }
}
