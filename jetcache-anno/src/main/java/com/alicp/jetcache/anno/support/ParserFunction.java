package com.alicp.jetcache.anno.support;

import java.util.function.Function;

/**
 * Created on 2022/04/06.
 *
 * @author huangli
 */
public class ParserFunction implements Function {

    private final String value;

    public ParserFunction(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public Object apply(Object t) {
        throw new UnsupportedOperationException();
    }
}
