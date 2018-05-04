/**
 * Created on 2018/3/27.
 */
package com.alicp.jetcache.anno.support;

import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class DefaultCacheNameGeneratorTest {

    private static final String[] hidePack = new String[]{"com.alicp.jetcache.anno"};

    interface I1 extends Serializable {
    }

    class C1 {
        public void foo() {
        }

        public String foo(I1 p) {
            return null;
        }

        public String foo2(I1[] p) {
            return null;
        }

        public void foo3(byte p2, short p3, char p4, int p5, long p6, float p7, double p8, boolean p9) {
        }
    }

    @Test
    public void testGenerateCacheName() throws Exception {
        DefaultCacheNameGenerator g = new DefaultCacheNameGenerator(hidePack);

        Method m1 = C1.class.getMethod("foo");
        Method m2 = C1.class.getMethod("foo", I1.class);
        Method m3 = C1.class.getMethod("foo2", I1[].class);
        Method m4 = C1.class.getMethod("foo3", byte.class, short.class, char.class, int.class, long.class, float.class, double.class, boolean.class);

        String s1 = "s.DefaultCacheNameGeneratorTest$C1." + m1.getName() + "()";
        String s2 = g.generateCacheName(m1, null);
        assertEquals(s1, s2);

        s1 = "s.DefaultCacheNameGeneratorTest$C1." + m2.getName() + "(Ls.DefaultCacheNameGeneratorTest$I1;)";
        s2 = g.generateCacheName(m2, null);
        assertEquals(s1, s2);

        g = new DefaultCacheNameGenerator(null);
        s1 = "c.a.j.a.s.DefaultCacheNameGeneratorTest$C1." + m3.getName() + "([Lc.a.j.a.s.DefaultCacheNameGeneratorTest$I1;)";
        s2 = g.generateCacheName(m3, null);
        assertEquals(s1, s2);

        g = new DefaultCacheNameGenerator(hidePack);
        s1 = "s.DefaultCacheNameGeneratorTest$C1." + m4.getName() + "(BSCIJFDZ)";
        s2 = g.generateCacheName(m4, null);
        assertEquals(s1, s2);
    }

    @Test
    public void removeHiddenPackageTest() {
        DefaultCacheNameGenerator g = new DefaultCacheNameGenerator(null);
        String[] hs = {"com.foo", "com.bar."};
        assertEquals("Foo", g.removeHiddenPackage(hs, "com.foo.Foo"));
        assertEquals("foo.Bar", g.removeHiddenPackage(hs, "com.bar.foo.Bar"));
        assertEquals("", g.removeHiddenPackage(hs, "com.foo"));
        assertEquals("com.bar.foo.Bar", g.removeHiddenPackage(null, "com.bar.foo.Bar"));
        assertEquals(null, g.removeHiddenPackage(hs, null));
    }
}
