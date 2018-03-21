package com.alicp.jetcache.support;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Created on 2016/10/8.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
@Disabled
public class FastjsonEncoderTest extends AbstractEncoderTest {
    @Test
    public void test() {
        //noinspection deprecation
        encoder = FastjsonValueEncoder.INSTANCE;
        //noinspection deprecation
        decoder = FastjsonValueDecoder.INSTANCE;
        super.baseTest();
    }

}
