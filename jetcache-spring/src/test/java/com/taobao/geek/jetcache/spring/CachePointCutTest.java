/**
 * Created on  13-09-22 18:46
 */
package com.taobao.geek.jetcache.spring;

import com.alibaba.fastjson.util.IdentityHashMap;
import com.taobao.geek.jetcache.Cached;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author yeli.hl
 */
//TODO 补充完整
public class CachePointCutTest {
    private CachePointcut pc;

    @Before
    public void setup(){
        pc = new CachePointcut();
        pc.setCacheConfigMap(new IdentityHashMap());
    }

    static interface I1 {
        @Cached
        int foo();
    }

    static class C1 implements I1{
        public int foo(){
           return 0;
        }
    }

    @Test
    public void testMatches1() throws Exception {
        Assert.assertTrue(pc.matches(I1.class.getMethod("foo"), C1.class));
        Assert.assertTrue(pc.matches(C1.class.getMethod("foo"), C1.class));
    }


    static interface I2 {
        int foo();
    }

    static class C2 implements I2{
        @Cached
        public int foo(){
            return 0;
        }
    }

    @Test
    public void testMatches2() throws Exception {
        Assert.assertTrue(pc.matches(I2.class.getMethod("foo"), C2.class));
        Assert.assertTrue(pc.matches(C2.class.getMethod("foo"), C2.class));
    }

}
