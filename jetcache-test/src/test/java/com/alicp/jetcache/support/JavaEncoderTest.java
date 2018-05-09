package com.alicp.jetcache.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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

        encoder = new JavaValueEncoder(false);
        decoder = new JavaValueDecoder(false);
        baseTest();
    }

    @Test
    public void compoundTest() {
        encoder = (p) -> JavaValueEncoder.INSTANCE.apply(JavaValueEncoder.INSTANCE.apply(p));
        decoder = (p) -> JavaValueDecoder.INSTANCE.apply((byte[]) JavaValueDecoder.INSTANCE.apply(p));
        baseTest();

        encoder = (p) -> JavaValueEncoder.INSTANCE.apply(KryoValueEncoder.INSTANCE.apply(p));
        decoder = (p) -> KryoValueDecoder.INSTANCE.apply((byte[]) JavaValueDecoder.INSTANCE.apply(p));
        baseTest();
    }

    @Test
    public void compatibleTest() {
        encoder = JavaValueEncoder.INSTANCE;
        decoder = KryoValueDecoder.INSTANCE;
        baseTest();
    }

    @Test
    public void errorTest() {
        encoder = JavaValueEncoder.INSTANCE;
        decoder = JavaValueDecoder.INSTANCE;
        byte[] bytes = encoder.apply("12345");
        bytes[0] = 0;
        assertThrows(CacheEncodeException.class, () -> decoder.apply(bytes));
        ((AbstractValueEncoder)encoder).writeHeader(bytes, KryoValueEncoder.IDENTITY_NUMBER);
        assertThrows(CacheEncodeException.class, () -> decoder.apply(bytes));

        encoder = JavaValueEncoder.INSTANCE;
        decoder = new JavaValueDecoder(false);
        assertThrows(CacheEncodeException.class, () -> decoder.apply(bytes));

        encoder = new JavaValueEncoder(false);
        decoder = JavaValueDecoder.INSTANCE;
        assertThrows(CacheEncodeException.class, () -> decoder.apply(bytes));
    }

    @Test
    public void gcTest() {
        encoder = JavaValueEncoder.INSTANCE;
        decoder = JavaValueDecoder.INSTANCE;
        super.gcTest();
    }

}
