package com.alicp.jetcache.redis.lettuce;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created on 2017/5/9.
 *
 * @author huangli
 */
public class JetCacheCodecTest {
    @Test
    public void testEncodeKey() {
        JetCacheCodec codec = new JetCacheCodec();
        byte[] bs = new byte[]{1, 2, 3};
        Assertions.assertArrayEquals(bs, (byte[]) codec.decodeKey(codec.encodeKey(bs)));
    }

    @Test
    public void testEncodeValue() {
        JetCacheCodec codec = new JetCacheCodec();
        byte[] bs = new byte[]{1, 2, 3};
        Assertions.assertArrayEquals(bs, (byte[]) codec.decodeValue(codec.encodeValue(bs)));

    }
}
