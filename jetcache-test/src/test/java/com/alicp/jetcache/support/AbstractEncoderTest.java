package com.alicp.jetcache.support;

import org.junit.Assert;

import java.util.Date;
import java.util.function.Function;

/**
 * Created on 2016/10/8.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class AbstractEncoderTest {

    protected Function<Object, byte[]> encoder;
    protected Function<byte[], Object> decoder;


    protected void baseTest() {
        Assert.assertEquals("123", decoder.apply(encoder.apply("123")));
        Assert.assertEquals(new Integer(123), decoder.apply(encoder.apply(123)));
        Date date = new Date();
        Assert.assertEquals(date, decoder.apply(encoder.apply(date)));
        Assert.assertArrayEquals(new byte[]{1, 2, 3, -1}, (byte[]) decoder.apply(encoder.apply(new byte[]{1, 2, 3, -1})));
        Assert.assertNull(decoder.apply(encoder.apply(null)));

        TestObject q = new TestObject();
        q.setId(100);
        q.setEmail("aaa");
        q.setName("bbb");
        q.setData(new byte[]{1,2,3});

        byte bs[] = encoder.apply(q);
        TestObject q2 = (TestObject) decoder.apply(bs);

        Assert.assertEquals(q.getId(), q2.getId());
        Assert.assertEquals(q.getName(), q2.getName());
        Assert.assertEquals(q.getEmail(), q2.getEmail());
        Assert.assertArrayEquals(new byte[]{1, 2, 3}, (byte[])q.getData());

        bs = encoder.apply(new Object[]{q, 123});
        Object[] o = (Object[]) decoder.apply(bs);
        q2 = (TestObject) o[0];
        Assert.assertEquals(123, o[1]);

        Assert.assertEquals(q.getId(), q2.getId());
        Assert.assertEquals(q.getName(), q2.getName());
        Assert.assertEquals(q.getEmail(), q2.getEmail());
        Assert.assertArrayEquals(new byte[]{1, 2, 3}, (byte[])q.getData());

    }
}
