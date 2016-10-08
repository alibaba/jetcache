package com.alicp.jetcache.support;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Created on 2016/10/8.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
@Ignore
public class FastjsonEncoderTest extends AbstractEncoderTest {
    @Test
    public void test() {
        encoder = FastjsonValueEncoder.INSTANCE;
        decoder = FastjsonValueDecoder.INSTANCE;
        super.doTest();
    }

}
