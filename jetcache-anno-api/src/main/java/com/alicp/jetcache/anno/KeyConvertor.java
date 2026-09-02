package com.alicp.jetcache.anno;

import java.util.function.Function;

/**
 * Created on 2016/12/12.
 *
 * A custom key convertor implements Function<Object, Object> is enough.
 * If a key convertor implements this interface, it can process byte[] and String, see AbstractExternalCache.
 *
 * @author huangli
 */
public interface KeyConvertor extends Function<Object, Object> {
    String NONE = "NONE";

    /**
     * Use fastjson2 as the key convertor implementation.
     * @since 2.8.0 Both FASTJSON and FASTJSON2 are mapped to the same fastjson2-based convertor, fastjson1 is removed.
     */
    String FASTJSON = "FASTJSON";

    /**
     * @since 2.6.5
     */
    String JACKSON = "JACKSON";

    /**
     * @since 2.7.9
     */
    String JACKSON3 = "JACKSON3";

    /**
     * Use fastjson2 as the key convertor implementation.
     * @since 2.8.0 Both FASTJSON and FASTJSON2 are mapped to the same fastjson2-based convertor, fastjson1 is removed.
     *
     * @since 2.7
     */
    String FASTJSON2 = "FASTJSON2";

    Function<Object, Object> NONE_INSTANCE = k -> k;
}
