/**
 * Created on  13-09-09 15:46
 */
package com.taobao.geek.jetcache.impl;

import com.taobao.geek.jetcache.CacheConfig;
import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class ClassUtilTest {

    interface I1 extends Serializable {
    }

    interface I2 {
    }

    interface I3 extends I1, I2 {
    }

    @Test
    public void testGetAllInterfaces() throws Exception {
        class C1 implements I3 {
        }

        class C2 extends C1 implements Cloneable, I1 {
        }
        Object obj = new C2();
        Class<?>[] is = ClassUtil.getAllInterfaces(obj);
        Assert.assertEquals(3, is.length);
    }

    class C1 {
        public void foo() {
        }

        public String foo(I1 p) {
            return null;
        }
    }

    @Test
    public void testGetSubArea() throws Exception {
        CacheConfig cc = new CacheConfig();
        Method m1 = C1.class.getMethod("foo");
        Method m2 = C1.class.getMethod("foo", I1.class);

        String s1 = cc.getVersion() + "_" + C1.class.getName() + "." + m1.getName() + "()V";
        String s2 = ClassUtil.getSubArea(cc, m1);
        Assert.assertEquals(s1, s2);

        s1 = cc.getVersion() + "_" + C1.class.getName() + "." + m1.getName() + "(L" + I1.class.getName().replace('.', '/') + ";)Ljava/lang/String;";
        s2 = ClassUtil.getSubArea(cc, m2);
        Assert.assertEquals(s1, s2);
    }

    @Test
    public void testGetMethodSig() throws Exception {
        Method m1 = C1.class.getMethod("foo");
        Method m2 = C1.class.getMethod("foo", I1.class);

        String s1 = m1.getName() + "()V";
        String s2 = ClassUtil.getMethodSig(m1);
        Assert.assertEquals(s1, s2);

        s1 = m1.getName() + "(L" + I1.class.getName().replace('.', '/') + ";)Ljava/lang/String;";
        s2 = ClassUtil.getMethodSig(m2);
        Assert.assertEquals(s1, s2);
    }

}
