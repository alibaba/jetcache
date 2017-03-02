package com.alicp.jetcache.support;

import com.alicp.jetcache.CacheValueHolder;
import org.junit.Assert;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;
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
        Assert.assertEquals(123, decoder.apply(encoder.apply(123)));
        Date date = new Date();
        Assert.assertEquals(date, decoder.apply(encoder.apply(date)));
        Assert.assertArrayEquals(new byte[]{1, 2, 3, -1}, (byte[]) decoder.apply(encoder.apply(new byte[]{1, 2, 3, -1})));
        Assert.assertNull(decoder.apply(encoder.apply(null)));

        TestObject q = new TestObject();
        q.setId(100);
        q.setEmail("aaa");
        q.setName("bbb");
        q.setData(new byte[]{1, 2, 3});
        Map<String, BigDecimal> m = new HashMap();
        m.put("12345", new BigDecimal(12345));
        q.setMap(m);

        byte bs[] = encoder.apply(q);
        TestObject q2 = (TestObject) decoder.apply(bs);

        compareTestObject(q, q2);


        bs = encoder.apply(new Object[]{q, 123});
        Object[] o = (Object[]) decoder.apply(bs);
        q2 = (TestObject) o[0];
        Assert.assertEquals(123, o[1]);

        compareTestObject(q, q2);

        A a = new A();
        a.setList(new ArrayList<>());
        a.getList().add(q);
        CacheValueHolder<CacheValueHolder<A>> h = new CacheValueHolder(
                new CacheValueHolder(a, System.currentTimeMillis(), 1000)
                , System.currentTimeMillis()
                , 1000);
        bs = encoder.apply(h);
        CacheValueHolder<CacheValueHolder<A>> h2 = (CacheValueHolder<CacheValueHolder<A>>) decoder.apply(bs);
        compareTestObject(h.getValue().getValue().getList().get(0)
                , h2.getValue().getValue().getList().get(0));
    }

    private void compareTestObject(TestObject q, TestObject q2) {
        Assert.assertEquals(q.getId(), q2.getId());
        Assert.assertEquals(q.getName(), q2.getName());
        Assert.assertEquals(q.getEmail(), q2.getEmail());
        Assert.assertArrayEquals(new byte[]{1, 2, 3}, (byte[]) q2.getData());
        Assert.assertEquals(new BigDecimal(12345), q2.getMap().get("12345"));
    }

    public static class A implements Serializable {
        private List<TestObject> list;

        public List<TestObject> getList() {
            return list;
        }

        public void setList(List<TestObject> list) {
            this.list = list;
        }
    }
}
