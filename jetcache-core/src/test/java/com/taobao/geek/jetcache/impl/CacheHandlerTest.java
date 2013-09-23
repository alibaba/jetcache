/**
 * Created on  13-09-23 09:29
 */
package com.taobao.geek.jetcache.impl;

import com.taobao.geek.jetcache.CacheConfig;
import com.taobao.geek.jetcache.CacheProviderFactory;
import com.taobao.geek.jetcache.Callback;
import com.taobao.geek.jetcache.support.CountClass;
import com.taobao.geek.jetcache.support.TestUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * @author yeli.hl
 */
//TODO 补充完整
public class CacheHandlerTest {

    private CacheProviderFactory cacheProviderFactory;
    private CacheConfig cacheConfig;
    private CountClass count;

    @Before
    public void setup(){
        cacheProviderFactory = TestUtil.getCacheProviderFactory();
        cacheConfig = new CacheConfig();
        count = new CountClass();
    }

    @Test
    public void testStaticInvoke1() throws Throwable {
        Method method = CountClass.class.getMethod("count");
        int x1, x2, x3;

        x1 = (Integer) CachedHandler.invoke(count, method, null, cacheProviderFactory, cacheConfig);
        x2 = (Integer) CachedHandler.invoke(count, method, null, cacheProviderFactory, cacheConfig);
        x3 = (Integer) CachedHandler.invoke(count, method, null, cacheProviderFactory, cacheConfig);
        Assert.assertEquals(x1, x2);
        Assert.assertEquals(x1, x3);
    }

    @Test
    public void testStaticInvoke2() throws Throwable {
        final Method method = CountClass.class.getMethod("count");
        int x1, x2, x3;

        x1 = (Integer) CachedHandler.invoke(count, method, null, cacheProviderFactory, null);
        x2 = (Integer) CachedHandler.invoke(count, method, null, cacheProviderFactory, null);
        x3 = (Integer) CachedHandler.invoke(count, method, null, cacheProviderFactory, null);
        Assert.assertTrue(x1 != x2 && x1 != x3 && x2 != x3);

        cacheConfig.setEnabled(false);
        x1 = (Integer) CachedHandler.invoke(count, method, null, cacheProviderFactory, cacheConfig);
        x2 = (Integer) CachedHandler.invoke(count, method, null, cacheProviderFactory, cacheConfig);
        x3 = (Integer) CachedHandler.invoke(count, method, null, cacheProviderFactory, cacheConfig);
        Assert.assertTrue(x1 != x2 && x1 != x3 && x2 != x3);

        CacheContextSupport.enableCache(new Callback() {
            @Override
            public void execute() throws Throwable {
                int x1 = (Integer) CachedHandler.invoke(count, method, null, cacheProviderFactory, cacheConfig);
                int x2 = (Integer) CachedHandler.invoke(count, method, null, cacheProviderFactory, cacheConfig);
                int x3 = (Integer) CachedHandler.invoke(count, method, null, cacheProviderFactory, cacheConfig);
                Assert.assertEquals(x1, x2);
                Assert.assertEquals(x1, x3);
            }
        });
    }

}
