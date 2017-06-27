package com.alicp.jetcache.support;

import org.junit.Test;

/**
 * Created on 2016/10/8.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class KryoEncoderTest extends AbstractEncoderTest {
    @Test
    public void test() {
        encoder = KryoValueEncoder.INSTANCE;
        decoder = KryoValueDecoder.INSTANCE;
        super.baseTest();

        encoder = (p) -> KryoValueEncoder.INSTANCE.apply(KryoValueEncoder.INSTANCE.apply(p));
        decoder = (p) -> KryoValueDecoder.INSTANCE.apply((byte[]) KryoValueDecoder.INSTANCE.apply(p));
        baseTest();

        encoder = (p) -> KryoValueEncoder.INSTANCE.apply(JavaValueEncoder.INSTANCE.apply(p));
        decoder = (p) -> JavaValueDecoder.INSTANCE.apply((byte[]) KryoValueDecoder.INSTANCE.apply(p));
        baseTest();
    }

}
