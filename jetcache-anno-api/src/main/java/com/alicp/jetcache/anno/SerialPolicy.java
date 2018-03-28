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

    Function<Object, byte[]> encoder();

    Function<byte[], Object> decoder();
}
