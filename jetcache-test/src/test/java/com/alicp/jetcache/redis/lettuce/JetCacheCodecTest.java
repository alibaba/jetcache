package com.alicp.jetcache.redis.lettuce;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created on 2017/5/9.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class JetCacheCodecTest {
    @Test
    public void testEncodeKey() {
        JetCacheCodec codec = new JetCacheCodec();
        byte[] bs = new byte[]{1, 2, 3};
        Assert.assertArrayEquals(bs, (byte[]) codec.decodeKey(codec.encodeKey(bs)));
    }

    @Test
    public void testEncodeValue() {
        JetCacheCodec codec = new JetCacheCodec();
        byte[] bs = new byte[]{1, 2, 3};
        Assert.assertArrayEquals(bs, (byte[]) codec.decodeValue(codec.encodeValue(bs)));

    }
}
