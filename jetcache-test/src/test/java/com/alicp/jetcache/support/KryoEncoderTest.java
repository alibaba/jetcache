package com.alicp.jetcache.support;

import com.alicp.jetcache.testsupport.DynamicQuery;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created on 2016/10/8.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class KryoEncoderTest {
    @Test
    public void test() {


        DynamicQuery q = new DynamicQuery();
        q.setId(100);
        q.setEmail("aaa");
        q.setName("bbb");

        byte bs[] = KryoValueEncoder.INSTANCE.apply(q);
        DynamicQuery q2 = (DynamicQuery) KryoValueDecoder.INSTANCE.apply(bs);

        Assert.assertEquals(q.getId(), q2.getId());
        Assert.assertEquals(q.getName(), q2.getName());
        Assert.assertEquals(q.getEmail(), q2.getEmail());

        Assert.assertEquals("123", KryoValueDecoder.INSTANCE.apply(KryoValueEncoder.INSTANCE.apply("123")));
        Assert.assertEquals(123, KryoValueDecoder.INSTANCE.apply(KryoValueEncoder.INSTANCE.apply(123)));

    }

}
