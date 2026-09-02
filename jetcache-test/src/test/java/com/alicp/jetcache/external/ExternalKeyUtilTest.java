package com.alicp.jetcache.external;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.Date;

/**
 * Created on 2016/12/28.
 *
 * @author huangli
 */
public class ExternalKeyUtilTest {

    private static class C implements Serializable {
        private static final long serialVersionUID = 3412272275328699372L;
        int a;
        String b;
    }

    @Test
    public void testBuildKey() throws Exception {
        Assertions.assertArrayEquals(buildKey("123"), buildKey(new String("123")));
        Assertions.assertArrayEquals(buildKey(new byte[]{1, 2, 3}), buildKey(new byte[]{1, 2, 3}));
        Assertions.assertArrayEquals(buildKey(123), buildKey(123));
        Assertions.assertArrayEquals(buildKey(123L), buildKey(123L));
        Assertions.assertArrayEquals(buildKey(true), buildKey(true));
        Assertions.assertArrayEquals(buildKey(new Date(123)), buildKey(new Date(123)));
        Assertions.assertArrayEquals(buildKey(new Date(123)), buildKey(new Date(123)));
        C c1 = new C();
        C c2 = new C();
        c1.a = 100;
        c1.b = "123";
        c2.a = 100;
        c2.b = "123";
        Assertions.assertArrayEquals(buildKey(c1), buildKey(c2));

        try {
            Assertions.assertArrayEquals(buildKey(123), buildKey(123L));
            Assertions.fail();
        } catch (Error e) {
        }

        try {
            Assertions.assertArrayEquals(buildKey(c1), buildKey(new C()));
            Assertions.fail();
        } catch (Error e) {
        }
    }

    private byte[] buildKey(Object key) throws Exception {
        return ExternalKeyUtil.buildKeyAfterConvert(key, "PRI");
    }
}
