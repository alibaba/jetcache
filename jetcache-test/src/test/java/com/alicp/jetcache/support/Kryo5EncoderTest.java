package com.alicp.jetcache.support;

import com.alicp.jetcache.anno.SerialPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Created on 2016/10/8.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class Kryo5EncoderTest extends AbstractEncoderTest {
    @Test
    public void test() {
        encoder = Kryo5ValueEncoder.INSTANCE;
        decoder = Kryo5ValueDecoder.INSTANCE;
        baseTest();

        encoder = new Kryo5ValueEncoder(false);
        decoder = new Kryo5ValueDecoder(false);
        baseTest();
    }

    @Test
    public void compoundTest() {
        encoder = (p) -> Kryo5ValueEncoder.INSTANCE.apply(Kryo5ValueEncoder.INSTANCE.apply(p));
        decoder = (p) -> Kryo5ValueDecoder.INSTANCE.apply((byte[]) Kryo5ValueDecoder.INSTANCE.apply(p));
        baseTest();

        encoder = (p) -> Kryo5ValueEncoder.INSTANCE.apply(JavaValueEncoder.INSTANCE.apply(p));
        decoder = (p) -> JavaValueDecoder.INSTANCE.apply((byte[]) Kryo5ValueDecoder.INSTANCE.apply(p));
        baseTest();
    }

    @Test
    public void compatibleTest() {
        encoder = Kryo5ValueEncoder.INSTANCE;
        decoder = JavaValueDecoder.INSTANCE;
        baseTest();
    }

    @Test
    public void errorTest() {
        encoder = Kryo5ValueEncoder.INSTANCE;
        decoder = Kryo5ValueDecoder.INSTANCE;
        byte[] bytes = encoder.apply("12345");
        bytes[0] = 0;
        assertThrows(CacheEncodeException.class, () -> decoder.apply(bytes));
        writeHeader(bytes, SerialPolicy.IDENTITY_NUMBER_JAVA);
        assertThrows(CacheEncodeException.class, () -> decoder.apply(bytes));

        encoder = Kryo5ValueEncoder.INSTANCE;
        decoder = new Kryo5ValueDecoder(false);
        assertThrows(CacheEncodeException.class, () -> decoder.apply(bytes));

        encoder = new Kryo5ValueEncoder(false);
        decoder = Kryo5ValueDecoder.INSTANCE;
        assertThrows(CacheEncodeException.class, () -> decoder.apply(bytes));
    }

    @Test
    public void gcTest() {
        encoder = Kryo5ValueEncoder.INSTANCE;
        decoder = Kryo5ValueDecoder.INSTANCE;
        super.gcTest();
    }

}
