/**
 * Created on  13-09-23 17:35
 */
package com.alicp.jetcache.anno.impl;

import com.alicp.jetcache.anno.CacheConsts;
import com.alicp.jetcache.anno.Cached;
import com.alicp.jetcache.anno.EnableCache;
import com.alicp.jetcache.anno.context.CacheContext;
import com.alicp.jetcache.anno.spring.MockRemoteCacheFactory;
import com.alicp.jetcache.anno.support.CacheAnnoConfig;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import com.alicp.jetcache.factory.EmbeddedCacheFactory;
import com.alicp.jetcache.support.FastjsonKeyConvertor;
import com.alicp.jetcache.support.KryoValueDecoder;
import com.alicp.jetcache.support.KryoValueEncoder;
import com.alicp.jetcache.testsupport.Count;
import com.alicp.jetcache.testsupport.CountClass;
import com.alicp.jetcache.testsupport.DynamicQuery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class ProxyUtilTest {

    private GlobalCacheConfig globalCacheConfig;
    private CacheAnnoConfig cacheAnnoConfig;

    @Before
    public void setup() {
        EmbeddedCacheFactory localFactory = new EmbeddedCacheFactory();
        localFactory.setKeyConvertor(FastjsonKeyConvertor.INSTANCE);
        Map localFactories = new HashMap();
        localFactories.put(CacheConsts.DEFAULT_AREA, localFactory);

        MockRemoteCacheFactory remoteFactory = new MockRemoteCacheFactory();
        remoteFactory.setKeyConvertor(FastjsonKeyConvertor.INSTANCE);
        remoteFactory.setValueEncoder(KryoValueEncoder.INSTANCE);
        remoteFactory.setValueDecoder(KryoValueDecoder.INSTANCE);
        Map remoteFactories = new HashMap();
        remoteFactories.put(CacheConsts.DEFAULT_AREA, remoteFactory);

        globalCacheConfig = new GlobalCacheConfig();
        globalCacheConfig.setLocalCacheFacotories(localFactories);
        globalCacheConfig.setRemoteCacheFacotories(remoteFactories);


        cacheAnnoConfig = new CacheAnnoConfig();
        CacheInvokeConfig cacheInvokeConfig = new CacheInvokeConfig();
        cacheInvokeConfig.setCacheAnnoConfig(cacheAnnoConfig);
    }

    @Test
    public void testGetProxy() {
        Count c1 = new CountClass();
        Count c2 = ProxyUtil.getProxy(c1, cacheAnnoConfig, globalCacheConfig);

        Assert.assertNotEquals(c1.count(), c1.count());
        Assert.assertEquals(c2.count(), c2.count());
        Assert.assertEquals(c2.count(100), c2.count(100));
        Assert.assertEquals(c2.count("S", 100), c2.count("S", 100));
        Assert.assertEquals(c2.count(new DynamicQuery(), 100), c2.count(new DynamicQuery(), 100));

        Assert.assertNotEquals(c2.count(200), c2.count(100));
    }

    interface I1 {
        int count();

        int countWithoutCache();
    }

    class C1 implements I1 {
        int count;

        @Cached
        public int count() {
            return count++;
        }

        public int countWithoutCache(){
            return count++;
        }
    }

    @Test
    //annotation on class
    public void testGetProxyByAnnotation1() {
        I1 c1 = new C1();
        I1 c2 = ProxyUtil.getProxyByAnnotation(c1, globalCacheConfig);
        Assert.assertNotEquals(c1.count(), c1.count());
        Assert.assertNotEquals(c1.countWithoutCache(), c1.countWithoutCache());
        Assert.assertEquals(c2.count(), c2.count());
        Assert.assertNotEquals(c2.countWithoutCache(), c2.countWithoutCache());
    }

    interface I2 {
        @Cached
        int count();
        int countWithoutCache();
    }

    class C2 implements I2 {
        int count;

        public int count() {
            return count++;
        }
        public int countWithoutCache(){
            return count++;
        }
    }
    class C22 implements I2 {
        int count;

        public int count() {
            return count++;
        }
        public int countWithoutCache(){
            return count++;
        }
    }

    @Test
    //annotation on intface
    public void testGetProxyByAnnotation2() {
        I2 c1 = new C2();
        I2 c2 = ProxyUtil.getProxyByAnnotation(c1, globalCacheConfig);

        Assert.assertNotEquals(c1.count(), c1.count());
        Assert.assertNotEquals(c1.countWithoutCache(), c1.countWithoutCache());
        Assert.assertEquals(c2.count(), c2.count());
        Assert.assertNotEquals(c2.countWithoutCache(), c2.countWithoutCache());

        I2 c3 = new C22();
        I2 c4 = ProxyUtil.getProxyByAnnotation(c3, globalCacheConfig);
        Assert.assertEquals(c2.count(), c4.count());
    }

    interface I3_1 {
        @Cached
        int count();
    }

    interface I3_2 extends I3_1 {
        int count();
        int countWithoutCache();
    }

    class C3 implements I3_2 {
        int count;

        public int count() {
            return count++;
        }
        public int countWithoutCache(){
            return count++;
        }

    }

    @Test
    //annotation on super interface
    public void testGetProxyByAnnotation3() {
        I3_2 c1 = new C3();
        I3_2 c2 = ProxyUtil.getProxyByAnnotation(c1, globalCacheConfig);
        Assert.assertNotEquals(c1.count(), c1.count());
        Assert.assertNotEquals(c1.countWithoutCache(), c1.countWithoutCache());
        Assert.assertEquals(c2.count(), c2.count());
        Assert.assertNotEquals(c2.countWithoutCache(), c2.countWithoutCache());
    }

    interface I4_1 {
        int count();
        int countWithoutCache();
    }

    interface I4_2 extends I4_1 {
        @Cached
        int count();
    }

    class C4 implements I4_2 {
        int count;

        public int count() {
            return count++;
        }
        public int countWithoutCache(){
            return count++;
        }
    }

    @Test
    //with super interface
    public void testGetProxyByAnnotation4() {
        I4_1 c1 = new C4();
        I4_1 c2 = ProxyUtil.getProxyByAnnotation(c1, globalCacheConfig);
        Assert.assertNotEquals(c1.count(), c1.count());
        Assert.assertNotEquals(c1.countWithoutCache(), c1.countWithoutCache());
        Assert.assertEquals(c2.count(), c2.count());
        Assert.assertNotEquals(c2.countWithoutCache(), c2.countWithoutCache());
    }

    interface I5 {
        int count();
        int countWithoutCache();
    }

    class C5 implements I5 {
        int count;

        @Cached(enabled = false)
        public int count() {
            return count++;
        }
        public int countWithoutCache(){
            return count++;
        }
    }

    @Test
    //enabled=false
    public void testGetProxyByAnnotation5() {
        I5 c1 = new C5();
        I5 c2 = ProxyUtil.getProxyByAnnotation(c1, globalCacheConfig);
        Assert.assertNotEquals(c1.count(), c1.count());
        Assert.assertNotEquals(c1.countWithoutCache(), c1.countWithoutCache());
        Assert.assertNotEquals(c2.count(), c2.count());
        Assert.assertNotEquals(c2.countWithoutCache(), c2.countWithoutCache());
        CacheContext.enableCache(() -> {
            Assert.assertNotEquals(c1.count(), c1.count());
            Assert.assertNotEquals(c1.countWithoutCache(), c1.countWithoutCache());
            Assert.assertEquals(c2.count(), c2.count());
            Assert.assertNotEquals(c2.countWithoutCache(), c2.countWithoutCache());
            return null;
        });
    }

    interface I6 {
        int count();
        int countWithoutCache();
    }

    class C6 implements I6 {
        int count;

        @EnableCache
        @Cached(enabled = false)
        public int count() {
            return count++;
        }
        public int countWithoutCache(){
            return count++;
        }
    }

    @Test
    //enabled=false+EnableCache
    public void testGetProxyByAnnotation6() {
        I6 c1 = new C6();
        I6 c2 = ProxyUtil.getProxyByAnnotation(c1, globalCacheConfig);
        Assert.assertNotEquals(c1.count(), c1.count());
        Assert.assertNotEquals(c1.countWithoutCache(), c1.countWithoutCache());
        Assert.assertEquals(c2.count(), c2.count());
        Assert.assertNotEquals(c2.countWithoutCache(), c2.countWithoutCache());
    }

    interface I7_1 {
        int count();
    }

    class C7_1 implements I7_1 {
        int count;
        @Cached(enabled = false)
        public int count() {
            return count++;
        }
    }

    interface I7_2 {
        int foo();
    }

    class C7_2 implements I7_2 {
        I7_1 service;

        @EnableCache
        public int foo() {
            return service.count();
        }
    }

    @Test
    //enabled=false+EnableCache（enable in caller）
    public void testGetProxyByAnnotation7() {
        I7_1 c1_1 = new C7_1();
        I7_1 c1_2 = ProxyUtil.getProxyByAnnotation(c1_1, globalCacheConfig);

        C7_2 c2_1 = new C7_2();
        c2_1.service = c1_2;
        I7_2 c2_2 = ProxyUtil.getProxyByAnnotation(c2_1, globalCacheConfig);
        Assert.assertNotEquals(c2_1.foo(), c2_1.foo());
        Assert.assertEquals(c2_2.foo(), c2_2.foo());
    }
}
