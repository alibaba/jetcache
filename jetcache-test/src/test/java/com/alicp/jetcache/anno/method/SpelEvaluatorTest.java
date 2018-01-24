/**
 * Created on 2018/1/19.
 */
package com.alicp.jetcache.anno.method;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class SpelEvaluatorTest {

    public void targetMethod(String p1, int p2) {
    }

    @Test
    public void test() throws Exception {
        RootObject root = new RootObject();
        root.setArgs(new Object[]{"123", 456});
        Method m = SpelEvaluatorTest.class.getMethod("targetMethod", String.class, int.class);

        SpelEvaluator e = new SpelEvaluator("bean('a')", m);
        assertEquals("a_bean", e.apply(root));

        e = new SpelEvaluator("#p1", m);
        assertEquals("123", e.apply(root));
        root = new RootObject();
        root.setArgs(new Object[]{null, 456});
        assertNull(e.apply(root));

    }

    public static class RootObject extends CacheInvokeContext {
        public String bean(String name) {
            return name + "_bean";
        }
    }
}

