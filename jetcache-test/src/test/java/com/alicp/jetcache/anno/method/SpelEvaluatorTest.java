/**
 * Created on 2018/1/19.
 */
package com.alicp.jetcache.anno.method;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class SpelEvaluatorTest {
    @Test
    public void test() {
        SpelEvaluator e = new SpelEvaluator("bean('a')");
        assertEquals("a_bean", e.apply(new RootObject()));
    }

    public static class RootObject {
        public String bean(String name) {
            return name + "_bean";
        }
    }
}


