/**
 * Created on 2019/6/13.
 */
package com.alicp.jetcache;

import com.alicp.jetcache.support.FastjsonKeyConvertor;
import com.alicp.jetcache.support.JavaValueDecoder;
import com.alicp.jetcache.support.JavaValueEncoder;
import com.alicp.jetcache.test.AbstractCacheTest;
import com.alicp.jetcache.external.MockRemoteCacheBuilder;
import org.junit.Test;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class MockRemoteCacheTest extends AbstractCacheTest {
    @Test
    public void Test() throws Exception {
        MockRemoteCacheBuilder b = new MockRemoteCacheBuilder();
        b.setKeyConvertor(FastjsonKeyConvertor.INSTANCE);
        b.setValueDecoder(JavaValueDecoder.INSTANCE);
        b.setValueEncoder(JavaValueEncoder.INSTANCE);
        cache = b.buildCache();
        baseTest();
    }
}
