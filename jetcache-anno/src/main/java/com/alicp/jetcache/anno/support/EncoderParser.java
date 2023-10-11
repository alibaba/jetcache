/**
 * Created on 2019/6/7.
 */
package com.alicp.jetcache.anno.support;

import java.util.function.Function;

/**
 * @author huangli
 */
public interface EncoderParser {
    Function<Object, byte[]> parseEncoder(String valueEncoder);
    Function<byte[], Object> parseDecoder(String valueDecoder);
}
