package com.alicp.jetcache.support;

import org.junit.Test;

/**
 * Created on 2016/10/8.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class JavaEncoderTest extends AbstractEncoderTest {
    @Test
    public void test() {
        encoder = JavaValueEncoder.INSTANCE;
        decoder = JavaValueDecoder.INSTANCE;
        super.doTest();
    }

}
