/**
 * Created on  13-09-23 09:29
 */
package com.taobao.geek.jetcache.impl;

import com.taobao.geek.jetcache.CacheConfig;
import com.taobao.geek.jetcache.CacheProviderFactory;
import com.taobao.geek.jetcache.support.CountClass;
import com.taobao.geek.jetcache.support.TestUtil;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * @author yeli.hl
 */
//TODO 补充完整
public class CacheHandlerTest {

    @Test
    public void testStaticInvoke() throws Throwable {
        Method method = CountClass.class.getMethod("count");
        CacheProviderFactory f = TestUtil.getCacheProviderFactory();
        CacheConfig cc = new CacheConfig();
        CountClass obj = new CountClass();
        int x1 = (Integer) CachedHandler.invoke(obj, method, null, f, cc);
        int x2 = (Integer) CachedHandler.invoke(obj, method, null, f, cc);
        int x3 = (Integer) CachedHandler.invoke(obj, method, null, f, cc);
        Assert.assertEquals(x1, x2);
        Assert.assertEquals(x1, x3);
    }

}
