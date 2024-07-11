/**
 * Created on  13-09-09 15:46
 */
package com.alicp.jetcache.anno.method;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * @author huangli
 */
public class ClassUtilTest {

    interface I1 extends Serializable {
    }

    interface I2 {
    }

    interface I3 extends I1, I2 {
    }

    class C1 {
        public void foo() {
            // Original foo method implementation
        }

        public String foo(I1 p) {
            // Original foo method implementation with different parameter
            return null;
        }

        public String foo2(I1 p) {
            // Original foo2 method implementation with different parameter
            return null;
        }

        // Refactored foo3 method using a parameter object
        public void foo3(Foo3Params params) {
            byte p2 = params.getP2();
            short p3 = params.getP3();
            char p4 = params.getP4();
            int p5 = params.getP5();
            long p6 = params.getP6();
            float p7 = params.getP7();
            double p8 = params.getP8();
            boolean p9 = params.isP9();

            // Method logic using parameters
        }
    }

    // Parameter object for foo3 method
    class Foo3Params {
        private byte p2;
        private short p3;
        private char p4;
        private int p5;
        private long p6;
        private float p7;
        private double p8;
        private boolean p9;

        public byte getP2() {
            return p2;
        }

        public void setP2(byte p2) {
            this.p2 = p2;
        }

        public short getP3() {
            return p3;
        }

        public void setP3(short p3) {
            this.p3 = p3;
        }

        public char getP4() {
            return p4;
        }

        public void setP4(char p4) {
            this.p4 = p4;
        }

        public int getP5() {
            return p5;
        }

        public void setP5(int p5) {
            this.p5 = p5;
        }

        public long getP6() {
            return p6;
        }

        public void setP6(long p6) {
            this.p6 = p6;
        }

        public float getP7() {
            return p7;
        }

        public void setP7(float p7) {
            this.p7 = p7;
        }

        public double getP8() {
            return p8;
        }

        public void setP8(double p8) {
            this.p8 = p8;
        }

        public boolean isP9() {
            return p9;
        }

        public void setP9(boolean p9) {
            this.p9 = p9;
        }
    }


    @Test
    public void testGetAllInterfaces() throws Exception {
        class CI1 implements I3 {
        }

        class CI2 extends CI1 implements Cloneable, I1 {
        }
        Object obj = new CI2();
        Class<?>[] is = ClassUtil.getAllInterfaces(obj);
        assertEquals(3, is.length);
    }

    @Test
    public void getShortClassNameTest() {
        assertNull(ClassUtil.getShortClassName(null));
        assertEquals("j.l.String", ClassUtil.getShortClassName("java.lang.String"));
        assertEquals("String", ClassUtil.getShortClassName("String"));
    }

    @Test
    public void testGetMethodSig() throws Exception {
        Method m1 = C1.class.getMethod("foo");
        Method m2 = C1.class.getMethod("foo", I1.class);
        Method m3 = C1.class.getMethod("foo2", I1.class);

        String s1 = m1.getName() + "()V";
        String s2 = ClassUtil.getMethodSig(m1);
        assertEquals(s1, s2);

        s1 = m2.getName() + "(L" + I1.class.getName().replace('.', '/') + ";)Ljava/lang/String;";
        s2 = ClassUtil.getMethodSig(m2);
        assertEquals(s1, s2);

        s1 = m3.getName() + "(L" + I1.class.getName().replace('.', '/') + ";)Ljava/lang/String;";
        s2 = ClassUtil.getMethodSig(m3);
        assertEquals(s1, s2);
    }

}
