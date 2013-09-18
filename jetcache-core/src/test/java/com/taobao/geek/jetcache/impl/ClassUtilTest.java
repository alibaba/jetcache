/**
 * Created on  13-09-09 15:46
 */
package com.taobao.geek.jetcache.impl;

import com.taobao.geek.jetcache.objectweb.asm.Type;
import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;

/**
 * @author yeli.hl
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
        System.out.println(Type.getType(ClassUtilTest.class).getDescriptor());
    }

}
