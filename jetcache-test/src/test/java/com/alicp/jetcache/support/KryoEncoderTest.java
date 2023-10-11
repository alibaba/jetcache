package com.alicp.jetcache.support;

import com.alicp.jetcache.VirtualThreadUtil;
import com.alicp.jetcache.anno.SerialPolicy;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Created on 2016/10/8.
 *
 * @author huangli
 */
public class KryoEncoderTest extends AbstractEncoderTest {
    @Test
    public void test() {
        encoder = KryoValueEncoder.INSTANCE;
        decoder = KryoValueDecoder.INSTANCE;
        baseTest();

        encoder = new KryoValueEncoder(false);
        decoder = new KryoValueDecoder(false);
        baseTest();
    }

    @Test
    public void compoundTest() {
        encoder = (p) -> KryoValueEncoder.INSTANCE.apply(KryoValueEncoder.INSTANCE.apply(p));
        decoder = (p) -> KryoValueDecoder.INSTANCE.apply((byte[]) KryoValueDecoder.INSTANCE.apply(p));
        baseTest();

        encoder = (p) -> KryoValueEncoder.INSTANCE.apply(JavaValueEncoder.INSTANCE.apply(p));
        decoder = (p) -> JavaValueDecoder.INSTANCE.apply((byte[]) KryoValueDecoder.INSTANCE.apply(p));
        baseTest();
    }

    @Test
    public void compatibleTest() {
        encoder = KryoValueEncoder.INSTANCE;
        decoder = JavaValueDecoder.INSTANCE;
        baseTest();
    }

    @Test
    public void errorTest() {
        encoder = KryoValueEncoder.INSTANCE;
        decoder = KryoValueDecoder.INSTANCE;
        byte[] bytes = encoder.apply("12345");
        bytes[0] = 0;
        assertThrows(CacheEncodeException.class, () -> decoder.apply(bytes));
        writeHeader(bytes, SerialPolicy.IDENTITY_NUMBER_JAVA);
        assertThrows(CacheEncodeException.class, () -> decoder.apply(bytes));

        encoder = KryoValueEncoder.INSTANCE;
        decoder = new KryoValueDecoder(false);
        assertThrows(CacheEncodeException.class, () -> decoder.apply(bytes));

        encoder = new KryoValueEncoder(false);
        decoder = KryoValueDecoder.INSTANCE;
        assertThrows(CacheEncodeException.class, () -> decoder.apply(bytes));
    }

    @Test
    public void gcTest() {
        encoder = KryoValueEncoder.INSTANCE;
        decoder = KryoValueDecoder.INSTANCE;
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
