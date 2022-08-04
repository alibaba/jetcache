package com.alicp.jetcache.support;

import com.alicp.jetcache.anno.SerialPolicy;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Created on 2016/10/8.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class Fastjson2EncoderTest extends AbstractEncoderTest {

    private void registerDecoder(Function<byte[], Object> decoder) {
        AbstractValueDecoder d = (AbstractValueDecoder) decoder;
        DecoderMap dm = new DecoderMap();
        dm.initDefaultDecoder();
        dm.register(SerialPolicy.IDENTITY_NUMBER_FASTJSON2, Fastjson2ValueDecoder.INSTANCE);
        d.setDecoderMap(dm);
    }

    @Test
    public void test() {
        encoder = Fastjson2ValueEncoder.INSTANCE;
        decoder = Fastjson2ValueDecoder.INSTANCE;
        registerDecoder(decoder);
        baseTest();

        encoder = new Fastjson2ValueEncoder(false);
        decoder = new Fastjson2ValueDecoder(false);
        baseTest();
    }

    @Test
    public void compatibleTest() {
        encoder = Fastjson2ValueEncoder.INSTANCE;
        decoder = Kryo5ValueDecoder.INSTANCE;
        registerDecoder(decoder);
        baseTest();

        encoder = Kryo5ValueEncoder.INSTANCE;
        decoder = Fastjson2ValueDecoder.INSTANCE;
        registerDecoder(decoder);
        baseTest();
    }

    @Test
    public void errorTest() {
        encoder = Fastjson2ValueEncoder.INSTANCE;
        decoder = Fastjson2ValueDecoder.INSTANCE;
        registerDecoder(decoder);
        byte[] bytes = encoder.apply("12345");
        bytes[0] = 0;
        assertThrows(CacheEncodeException.class, () -> decoder.apply(bytes));
        writeHeader(bytes, SerialPolicy.IDENTITY_NUMBER_JAVA);
        assertThrows(CacheEncodeException.class, () -> decoder.apply(bytes));

        encoder = Fastjson2ValueEncoder.INSTANCE;
        decoder = new JavaValueDecoder(false);
        assertThrows(CacheEncodeException.class, () -> decoder.apply(bytes));

        encoder = new Fastjson2ValueEncoder(false);
        decoder = Fastjson2ValueDecoder.INSTANCE;
        assertThrows(CacheEncodeException.class, () -> decoder.apply(bytes));
    }

    @Test
    public void gcTest() {
        encoder = Fastjson2ValueEncoder.INSTANCE;
        decoder = Fastjson2ValueDecoder.INSTANCE;
        registerDecoder(decoder);
        super.gcTest();
    }

}
