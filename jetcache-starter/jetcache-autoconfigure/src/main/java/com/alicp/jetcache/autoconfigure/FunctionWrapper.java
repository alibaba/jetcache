package com.alicp.jetcache.autoconfigure;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created on 2021/06/25.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
class FunctionWrapper<T, R> implements Function<T, R> {

    private final Supplier<Function<T, R>> supplier;
    private transient Function<T, R> target;

    public FunctionWrapper(Supplier<Function<T, R>> supplier) {
        this.supplier = supplier;
    }

    @Override
    public R apply(T t) {
        if (target == null) {
            synchronized (this) {
                if (target == null) {
                    target = supplier.get();
                }
            }
        }
        return target.apply(t);
    }
}
