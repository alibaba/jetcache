/**
 * Created on  13-11-01 10:53
 */
package com.alicp.jetcache.anno.impl;

import com.alicp.jetcache.anno.SerialPolicy;
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

//    @Test
    public void testPerformance() throws Exception {
        int count = 500000;
        long t = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            testPerformance(SerialPolicy.JAVA);
        }
        System.out.println((System.currentTimeMillis() - t));
    }

    private void testPerformance(String serialPolicy) throws Exception{
        A a = new A();
        B b = new B();
        a.b = b;
        b.a = a;
        a.map.put("a", a);
        a.map.put("b", b);

        byte[] bs1 = SerializeUtil.encode(a, serialPolicy);
        @SuppressWarnings("UnusedAssignment")
        A a2 = (A) SerializeUtil.decode(bs1);
    }

    private void test(String p) throws Exception {
        A a = new A();
        B b = new B();
        a.b = b;
        b.a = a;
        if (!SerialPolicy.FASTJSON.equals(p)) {
            a.map.put("a", a);
            a.map.put("b", b);
        }

        byte[] bs1 = SerializeUtil.encode(a, p);
        byte[] bs2 = SerializeUtil.encode(b, p);

        A a2 = (A) SerializeUtil.decode(bs1);
        B b2 = (B) SerializeUtil.decode(bs2);

        Assert.assertEquals(a.f1, a2.f1);
        Assert.assertNotNull(a2.b);
        Assert.assertSame(a2, a2.b.a);

        if (!SerialPolicy.FASTJSON.equals(p)) {
            Assert.assertSame(a2, a2.map.get("a"));
            Assert.assertSame(a2, ((B) a2.map.get("b")).a);
            Assert.assertEquals(b.f2, ((B) a2.map.get("b")).f2);
            Assert.assertEquals(b.f3, ((B) a2.map.get("b")).f3);
        }

        Assert.assertEquals(b.f2, b2.f2);
        Assert.assertEquals(b.f3, b2.f3);
        Assert.assertNotNull(b2.a);
        Assert.assertNotNull(b2.a.map);
        Assert.assertSame(b2, b2.a.b);
        if (!SerialPolicy.FASTJSON.equals(p)) {
            Assert.assertSame(b2, b2.a.map.get("b"));
            Assert.assertSame(b2.a, b2.a.map.get("a"));
        }
    }
}
