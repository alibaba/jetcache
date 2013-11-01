/**
 * Created on  13-11-01 10:53
 */
package com.taobao.geek.jetcache.impl;

import com.taobao.geek.jetcache.SerialPolicy;
import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class SerializeUtilTest {
    public static class A implements Serializable {
        private int f1 = 100;
        private HashMap map = new HashMap();
        private B b;

        public int getF1() {
            return f1;
        }

        public void setF1(int f1) {
            this.f1 = f1;
        }

        public HashMap getMap() {
            return map;
        }

        public void setMap(HashMap map) {
            this.map = map;
        }

        public B getB() {
            return b;
        }

        public void setB(B b) {
            this.b = b;
        }
    }

    private static class B implements Serializable {
        private int f2 = 1000;
        private String f3 = "23saldfjaldsfjsd;f汉字";
        private A a;

        public int getF2() {
            return f2;
        }

        public void setF2(int f2) {
            this.f2 = f2;
        }

        public A getA() {
            return a;
        }

        public void setA(A a) {
            this.a = a;
        }
    }

    @Test
    public void testJava() throws Exception {
        test(SerialPolicy.JAVA);
    }

    @Test
    public void testFastjson() throws Exception {
        test(SerialPolicy.FASTJSON);
    }

    @Test
    public void testKryo() throws Exception {
        test(SerialPolicy.KRYO);
    }

    private void test(SerialPolicy p) throws Exception {
        A a = new A();
        B b = new B();
        a.b = b;
        b.a = a;
        a.map.put("a", a);
        a.map.put("b", b);

        A a2 = (A) SerializeUtil.decode(SerializeUtil.encode(a, p));
        B b2 = (B) SerializeUtil.decode(SerializeUtil.encode(b, p));

        Assert.assertEquals(a.f1, a2.f1);
        Assert.assertNotNull(a2.b);
        Assert.assertSame(a2, a2.b.a);
        Assert.assertSame(a2, a2.map.get("a"));
        Assert.assertSame(a2, ((B) a2.map.get("b")).a);
        Assert.assertEquals(b.f2, ((B) a2.map.get("b")).f2);
        Assert.assertEquals(b.f3, ((B) a2.map.get("b")).f3);

        Assert.assertEquals(b.f2, b2.f2);
        Assert.assertEquals(b.f3, b2.f3);
        Assert.assertNotNull(b2.a);
        Assert.assertNotNull(b2.a.map);
        Assert.assertSame(b2, b2.a.b);
        Assert.assertSame(b2, b2.a.map.get("b"));
        Assert.assertSame(b2.a, b2.a.map.get("a"));
    }
}
