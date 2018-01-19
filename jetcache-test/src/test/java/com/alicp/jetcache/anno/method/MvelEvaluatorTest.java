/**
 * Created on 2018/1/19.
 */
package com.alicp.jetcache.anno.method;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class MvelEvaluatorTest {
    @Test
    public void test() {
        MvelEvaluator e = new MvelEvaluator("bean('a')");
        assertEquals("a_bean", e.apply(new RootObject()));
    }

    public static class RootObject {
        public String bean(String name) {
            return name + "_bean";
        }
    }
}


