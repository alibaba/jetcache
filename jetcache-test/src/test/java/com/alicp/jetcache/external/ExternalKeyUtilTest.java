package com.alicp.jetcache.external;

import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.util.Date;

/**
 * Created on 2016/12/28.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class ExternalKeyUtilTest {

    private static class C implements Serializable {
        private static final long serialVersionUID = 3412272275328699372L;
        int a;
        String b;
    }

    @Test
    public void testBuildKey() throws Exception {
        Assert.assertArrayEquals(buildKey("123"), buildKey(new String("123")));
        Assert.assertArrayEquals(buildKey(new byte[]{1, 2, 3}), buildKey(new byte[]{1, 2, 3}));
        Assert.assertArrayEquals(buildKey(123), buildKey(123));
        Assert.assertArrayEquals(buildKey(123L), buildKey(123L));
        Assert.assertArrayEquals(buildKey(true), buildKey(true));
        Assert.assertArrayEquals(buildKey(new Date(123)), buildKey(new Date(123)));
        Assert.assertArrayEquals(buildKey(new Date(123)), buildKey(new Date(123)));
        C c1 = new C();
        C c2 = new C();
        c1.a = 100;
        c1.b = "123";
        c2.a = 100;
        c2.b = "123";
        Assert.assertArrayEquals(buildKey(c1), buildKey(c2));

        try {
            Assert.assertArrayEquals(buildKey(123), buildKey(123L));
            Assert.fail();
        } catch (Error e) {
        }

        try {
            Assert.assertArrayEquals(buildKey(c1), buildKey(new C()));
            Assert.fail();
        } catch (Error e) {
        }
    }

    private byte[] buildKey(Object key) throws Exception {
        return ExternalKeyUtil.buildKeyAfterConvert(key, "PRI");
    }
}
