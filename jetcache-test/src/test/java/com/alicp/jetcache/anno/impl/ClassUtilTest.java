/**
 * Created on  13-09-09 15:46
 */
package com.alicp.jetcache.anno.impl;

import com.alicp.jetcache.anno.support.CacheAnnoConfig;
import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
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
    }

    @Test
    public void testGetAllInterfaces() throws Exception {
        class CI1 implements I3 {
        }

        class CI2 extends CI1 implements Cloneable, I1 {
        }
        Object obj = new CI2();
        Class<?>[] is = ClassUtil.getAllInterfaces(obj);
        Assert.assertEquals(3, is.length);
    }

    @Test
    public void testGetSubArea() throws Exception {
        Method m1 = C1.class.getMethod("foo");
        Method m2 = C1.class.getMethod("foo", I1.class);
        Method m3 = C1.class.getMethod("foo2", I1.class);

        String s1 = "1_impl.ClassUtilTest$C1." + m1.getName() + "()V";
        String s2 = ClassUtil.getSubArea(1, m1, hidePack);
        Assert.assertEquals(s1, s2);

        s1 = "1_impl.ClassUtilTest$C1." + m2.getName() + "(Limpl/ClassUtilTest$I1;)Ljava/lang/String;";
        s2 = ClassUtil.getSubArea(1, m2, hidePack);
        Assert.assertEquals(s1, s2);

        s1 = "1_" + C1.class.getName() + "." + m3.getName() + "(L" + I1.class.getName().replace('.', '/') + ";)Ljava/lang/String;";
        s2 = ClassUtil.getSubArea(1, m3, null);
        Assert.assertEquals(s1, s2);
    }

    @Test
    public void testGetMethodSig() throws Exception {
        Method m1 = C1.class.getMethod("foo");
        Method m2 = C1.class.getMethod("foo", I1.class);
        Method m3 = C1.class.getMethod("foo2", I1.class);

        String s1 = m1.getName() + "()V";
        String s2 = ClassUtil.getMethodSig(m1, hidePack);
        Assert.assertEquals(s1, s2);

        s1 = m2.getName() + "(Limpl/ClassUtilTest$I1;)Ljava/lang/String;";
        s2 = ClassUtil.getMethodSig(m2, hidePack);
        Assert.assertEquals(s1, s2);

        s1 = m3.getName() + "(L" + I1.class.getName().replace('.', '/') + ";)Ljava/lang/String;";
        s2 = ClassUtil.getMethodSig(m3, null);
        Assert.assertEquals(s1, s2);
    }

}
