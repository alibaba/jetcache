package com.alicp.jetcache.anno;

import java.util.function.Function;

/**
 * Created on 2016/12/12.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public interface KeyConvertor {
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
