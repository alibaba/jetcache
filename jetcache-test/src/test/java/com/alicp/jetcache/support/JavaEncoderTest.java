package com.alicp.jetcache.support;

import com.alicp.jetcache.anno.SerialPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Created on 2016/10/8.
 *
 * @author huangli
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
        writeHeader(bytes, SerialPolicy.IDENTITY_NUMBER_KRYO4);
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


    @Test
    public void testVirtualThreadPool() throws InterruptedException {
       testByThreadPool(true,-1,100,this::test);
    }

    @Test
    public void testVirtualThreadGC() throws InterruptedException {
        testByThreadPool(true,-1,100,this::gcTest);
    }

    @Test
    public void testFixThreadPool() throws InterruptedException {
        testByThreadPool(false,3,100,this::test);
    }

    @Test
    public void testFixThreadGC() throws InterruptedException {
        testByThreadPool(false,3,100,this::gcTest);
    }

}
