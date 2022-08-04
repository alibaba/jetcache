/**
 * Created on  13-10-31 22:46
 */
package com.alicp.jetcache.anno;

import java.util.function.Function;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public interface SerialPolicy {
    String JAVA = "JAVA";

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

    int IDENTITY_NUMBER_JAVA = 0x4A953A80;
    // int IDENTITY_NUMBER_FASTJSON = 0x4A953A81; not used since 2.5+
    int IDENTITY_NUMBER_KRYO4 = 0x4A953A82;

    /**
     * @since 2.7
     */
    int IDENTITY_NUMBER_KRYO5 = 0xF6E0A5C0;

    /**
     * fastjson2 encoder/decoder is implemented but not register by default.
     * This is because json is not good serializable util for java and has many compatible problems.
     *
     * @see com.alicp.jetcache.anno.support.DefaultEncoderParser
     * @see com.alicp.jetcache.support.DecoderMap
     * @since 2.7
     */
    int IDENTITY_NUMBER_FASTJSON2 = 0xF6E0A5C1;

    Function<Object, byte[]> encoder();

    Function<byte[], Object> decoder();
}
