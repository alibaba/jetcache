/**
 * Created on 2018/1/23.
 */
package com.alicp.jetcache.anno.method;

import com.alicp.jetcache.CacheConfigException;
import com.alicp.jetcache.anno.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class CacheConfigUtilTest {

    interface I {
        @Cached
        void m1();

        @Cached
        @CacheRefresh(refresh = 100)
        void m1_2();

        @EnableCache
        void m2();

        @CacheInvalidate(name = "foo")
        void m3();

        @CacheUpdate(name = "foo", value = "bar")
        void m4();

        @Cached
        @CacheInvalidate(name = "foo")
        void m5();

        @Cached
        @CacheUpdate(name = "foo", value = "bar")
        void m6();

        @CacheInvalidate(name = "foo")
        @CacheUpdate(name = "foo", value = "bar")
        void m7();

        @CacheInvalidate(name = "foo")
        @CacheInvalidate(name = "bar")
        void m8();
    }

    @Test
    public void test() throws Exception {
        CacheInvokeConfig cic = new CacheInvokeConfig();
        CacheConfigUtil.parse(cic, I.class.getMethod("m1"));
        assertNotNull(cic.getCachedAnnoConfig());
        assertNull(cic.getCachedAnnoConfig().getRefreshPolicy());

        cic = new CacheInvokeConfig();
        CacheConfigUtil.parse(cic, I.class.getMethod("m1_2"));
        assertNotNull(cic.getCachedAnnoConfig());
        assertNotNull(cic.getCachedAnnoConfig().getRefreshPolicy());


        cic = new CacheInvokeConfig();
        CacheConfigUtil.parse(cic, I.class.getMethod("m2"));
        assertTrue(cic.isEnableCacheContext());

        cic = new CacheInvokeConfig();
        CacheConfigUtil.parse(cic, I.class.getMethod("m3"));
        assertNotNull(cic.getInvalidateAnnoConfigs());

        cic = new CacheInvokeConfig();
        CacheConfigUtil.parse(cic, I.class.getMethod("m4"));
        assertNotNull(cic.getUpdateAnnoConfig());

        CacheInvokeConfig cic2 = new CacheInvokeConfig();
        assertThrows(CacheConfigException.class, () -> CacheConfigUtil.parse(cic2, I.class.getMethod("m5")));
        assertThrows(CacheConfigException.class, () -> CacheConfigUtil.parse(cic2, I.class.getMethod("m6")));

        cic = new CacheInvokeConfig();
        CacheConfigUtil.parse(cic, I.class.getMethod("m7"));
        assertNotNull(cic.getInvalidateAnnoConfigs());
        assertNotNull(cic.getUpdateAnnoConfig());

        cic = new CacheInvokeConfig();
        CacheConfigUtil.parse(cic, I.class.getMethod("m8"));
        assertNotNull(cic.getInvalidateAnnoConfigs());
        assertEquals(2, cic.getInvalidateAnnoConfigs().size());
    }
}
