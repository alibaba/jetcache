/**
 * Created on  13-10-31 22:46
 */
package com.alicp.jetcache.anno;

import java.util.function.Function;

/**
 * @author huangli
 */
public interface SerialPolicy {
    String JAVA = "JAVA";

    /**
     * since 2.8.0, the KRYO also use KRYO5 implementation, so KRYO and KRYO5 are same.
     */
    String KRYO = "KRYO";

    /**
     * @since 2.7
     */
    String KRYO5 = "KRYO5";

    /**
     * fastjson2 encoder/decoder is implemented but not register by default.
     * This is because json is not good serializable util for java and has many compatible problems.
     *
     * @see com.alicp.jetcache.anno.support.DefaultEncoderParser
     * @see com.alicp.jetcache.support.DecoderMap
     * @since 2.7
     */
    String FASTJSON2 = "FASTJSON2";

    /**
     * jackson3 encoder/decoder is implemented but not register by default.
     * This is because json is not good serializable util for java and has many compatible problems.
     *
     * @see com.alicp.jetcache.anno.support.DefaultEncoderParser
     * @see com.alicp.jetcache.support.DecoderMap
     * @since 2.8
     */
    String JACKSON3 = "JACKSON3";

    Function<Object, byte[]> encoder();

    Function<byte[], Object> decoder();
}
