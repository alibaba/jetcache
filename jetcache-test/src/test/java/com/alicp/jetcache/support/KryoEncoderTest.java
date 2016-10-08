package com.alicp.jetcache.support;

import com.alicp.jetcache.testsupport.DynamicQuery;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created on 2016/10/8.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class KryoEncoderTest extends AbstractEncoderTest {
    @Test
    public void test() {
        encoder = KryoValueEncoder.INSTANCE;
        decoder = KryoValueDecoder.INSTANCE;
        super.doTest();
    }

}
