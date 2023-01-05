package com.alicp.jetcache.anno;

import java.util.function.Function;

/**
 * Created on 2016/12/12.
 *
 * A custom key convertor implements Function<Object, Object> is enough.
 * If a key convertor implements this interface, it can process byte[] and String, see AbstractExternalCache.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public interface KeyConvertor extends Function<Object, Object> {
    String NONE = "NONE";
    String FASTJSON = "FASTJSON";

    /**
     * @since 2.6.5
     */
    String JACKSON = "JACKSON";

    /**
     * @since 2.7
     */
    String FASTJSON2 = "FASTJSON2";

    Function<Object, Object> NONE_INSTANCE = k -> k;
}
