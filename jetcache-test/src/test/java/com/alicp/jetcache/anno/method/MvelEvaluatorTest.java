/**
 * Created on 2018/1/19.
 */
package com.alicp.jetcache.anno.method;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author huangli
 */
public class MvelEvaluatorTest {
    @Test
    @DisabledForJreRange(min = JRE.JAVA_21, disabledReason = "mvel not work on java 21 now")
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


