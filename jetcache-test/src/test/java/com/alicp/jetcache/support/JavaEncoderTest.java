package com.alicp.jetcache.support;

import org.junit.Test;

/**
 * Created on 2016/10/8.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class JavaEncoderTest extends AbstractEncoderTest {
    @Test
    public void test() {
        encoder = JavaValueEncoder.INSTANCE;
        decoder = JavaValueDecoder.INSTANCE;
        baseTest();

        encoder = (p) -> JavaValueEncoder.INSTANCE.apply(JavaValueEncoder.INSTANCE.apply(p));
        decoder = (p) -> JavaValueDecoder.INSTANCE.apply((byte[]) JavaValueDecoder.INSTANCE.apply(p));
        baseTest();

        encoder = (p) -> JavaValueEncoder.INSTANCE.apply(KryoValueEncoder.INSTANCE.apply(p));
        decoder = (p) -> KryoValueDecoder.INSTANCE.apply((byte[]) JavaValueDecoder.INSTANCE.apply(p));
        baseTest();
    }

}
