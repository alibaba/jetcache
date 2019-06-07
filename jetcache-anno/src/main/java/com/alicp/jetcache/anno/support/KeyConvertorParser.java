/**
 * Created on 2019/6/7.
 */
package com.alicp.jetcache.anno.support;

import java.util.function.Function;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public interface KeyConvertorParser {
    Function<Object, Object> parseKeyConvertor(String convertor);
}
