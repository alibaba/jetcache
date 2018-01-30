/**
 * Created on  13-09-09 15:46
 */
package com.alicp.jetcache.anno.method;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class ClassUtilTest {

    private static final String[] hidePack = new String[]{"com.alicp.jetcache.anno"};

    interface I1 extends Serializable {
    }

    interface I2 {
    }

    interface I3 extends I1, I2 {
    }

    class C1 {
        public void foo() {
        }

        public String foo(I1 p) {
            return null;
        }

        public String foo2(I1 p) {
            return null;
        }

        public void foo3(byte p2, short p3, char p4, int p5, long p6, float p7, double p8, boolean p9) {
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
    public void testGenerateCacheName() throws Exception {
        Method m1 = C1.class.getMethod("foo");
        Method m2 = C1.class.getMethod("foo", I1.class);
        Method m3 = C1.class.getMethod("foo2", I1.class);
        Method m4 = C1.class.getMethod("foo3", byte.class, short.class, char.class, int.class, long.class, float.class, double.class, boolean.class);

        String s1 = "m.ClassUtilTest$C1." + m1.getName() + "()";
        String s2 = ClassUtil.generateCacheName(m1, hidePack);
        assertEquals(s1, s2);

        s1 = "m.ClassUtilTest$C1." + m2.getName() + "(Lm.ClassUtilTest$I1;)";
        s2 = ClassUtil.generateCacheName(m2, hidePack);
        assertEquals(s1, s2);

        s1 = "c.a.j.a.m.ClassUtilTest$C1." + m3.getName() + "(Lc.a.j.a.m.ClassUtilTest$I1;)";
        s2 = ClassUtil.generateCacheName(m3, null);
        assertEquals(s1, s2);

        s1 = "m.ClassUtilTest$C1." + m4.getName() + "(BSCIJFDZ)";
        s2 = ClassUtil.generateCacheName(m4, hidePack);
        assertEquals(s1, s2);
    }

    @Test
    public void removeHiddenPackageTest() {
        String[] hs = {"com.foo", "com.bar."};
        assertEquals("Foo", ClassUtil.removeHiddenPackage(hs, "com.foo.Foo"));
        assertEquals("foo.Bar", ClassUtil.removeHiddenPackage(hs, "com.bar.foo.Bar"));
        assertEquals("", ClassUtil.removeHiddenPackage(hs, "com.foo"));
        assertEquals("com.bar.foo.Bar", ClassUtil.removeHiddenPackage(null, "com.bar.foo.Bar"));
        assertEquals(null, ClassUtil.removeHiddenPackage(hs, null));
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
