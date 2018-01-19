/**
 * Created on 2018/1/19.
 */
package com.alicp.jetcache.anno.method;

import com.alicp.jetcache.CacheConfigException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class ExpressionEvaluatorTest {
    @Test
    public void test() {
        ExpressionEvaluator e = new ExpressionEvaluator("1+1");
        assertTrue(e.getTarget() instanceof SpelEvaluator);
        e = new ExpressionEvaluator("spel{1+1}");
        assertTrue(e.getTarget() instanceof SpelEvaluator);
        e = new ExpressionEvaluator("mvel{1+1}");
        assertTrue(e.getTarget() instanceof MvelEvaluator);
        assertThrows(CacheConfigException.class,() -> new ExpressionEvaluator("xxx{1+1}"));
    }
}


