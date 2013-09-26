/**
 * Created on  13-09-23 17:02
 */
package com.taobao.geek.jetcache.impl;

import com.taobao.geek.jetcache.support.DynamicQuery;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class DefaultKeyGeneratorTest {
    static class C extends DynamicQuery {
        private C mate;

        public C getMate() {
            return mate;
        }

        public void setMate(C mate) {
            this.mate = mate;
        }
    }

    @Test
    public void test() {
        DefaultKeyGenerator g = new DefaultKeyGenerator();
        String k1, k2, k3;

        k1 = g.getKey(new Object[]{10, 20, 10});
        k2 = g.getKey(new Object[]{10, 20, 10});
        k3 = g.getKey(new Object[]{20, 10, 10});
        Assert.assertEquals(k1, k2);
        Assert.assertNotEquals(k1, k3);

        k1 = g.getKey(new Object[]{10, null});
        k2 = g.getKey(new Object[]{10, ""});
        Assert.assertNotEquals(k1, k2);

        C q1 = new C();
        C q2 = new C();
        Assert.assertEquals(g.getKey(new Object[]{q1}), g.getKey(new Object[]{q2}));

        q1.setId(10000);
        q2.setId(10000);
        q1.setName("N1");
        Assert.assertNotEquals(g.getKey(new Object[]{q1}), g.getKey(new Object[]{q2}));
        q2.setName("N1");
        Assert.assertEquals(g.getKey(new Object[]{q1}), g.getKey(new Object[]{q2}));

        q1.setMate(q2);
        Assert.assertNotEquals(g.getKey(new Object[]{q1}), g.getKey(new Object[]{q2}));
        q2.setMate(q1);
        Assert.assertEquals(g.getKey(new Object[]{q1}), g.getKey(new Object[]{q2}));

        C q3 = new C();
        q3.setId(20);
        q1.setMate(q3);
        Assert.assertNotEquals(g.getKey(new Object[]{q1}), g.getKey(new Object[]{q2}));
        q2.setMate(q3);
        Assert.assertEquals(g.getKey(new Object[]{q1}), g.getKey(new Object[]{q2}));
    }
}
