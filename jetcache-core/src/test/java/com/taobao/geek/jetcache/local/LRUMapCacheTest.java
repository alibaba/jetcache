/**
 * Created on  13-09-24 10:20
 */
package com.taobao.geek.jetcache.local;

import com.taobao.geek.jetcache.support.CacheConfig;
import com.taobao.geek.jetcache.support.CacheResultCode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class LRUMapCacheTest {
    private LRUMapCache cache;
    private CacheConfig cc;

    @Before
    public void setup() {
        cache = new LRUMapCache();
        cc = new CacheConfig();
    }

    @Test
    public void test1() throws Exception {
        Assert.assertEquals(CacheResultCode.NOT_EXISTS, cache.get(cc, "S1", "K1").getResultCode());
        Assert.assertEquals(CacheResultCode.SUCCESS, cache.put(cc, "S1", "K1", "V1"));
        Assert.assertEquals(CacheResultCode.SUCCESS, cache.get(cc, "S1", "K1").getResultCode());
        Assert.assertEquals("V1", cache.get(cc, "S1", "K1").getValue());
        Assert.assertEquals(CacheResultCode.NOT_EXISTS, cache.get(cc, "S2", "K1").getResultCode());
        Assert.assertEquals(CacheResultCode.SUCCESS, cache.put(cc, "S1", "K1", "V2"));
        Assert.assertEquals("V2", cache.get(cc, "S1", "K1").getValue());

        cc.setArea("A2");
        Assert.assertEquals(CacheResultCode.NOT_EXISTS, cache.get(cc, "S1", "K1").getResultCode());
        Assert.assertEquals(CacheResultCode.SUCCESS, cache.put(cc, "S1", "K1", "V1"));
        Assert.assertEquals(CacheResultCode.SUCCESS, cache.get(cc, "S1", "K1").getResultCode());
        Assert.assertEquals("V1", cache.get(cc, "S1", "K1").getValue());

    }

    @Test
    public void testLRU1(){
        cc.setLocalLimit(2);
        Assert.assertEquals(CacheResultCode.SUCCESS, cache.put(cc, "S1", "K1", "V1"));
        Assert.assertEquals(CacheResultCode.SUCCESS, cache.put(cc, "S1", "K2", "V2"));
        Assert.assertEquals(CacheResultCode.SUCCESS, cache.put(cc, "S1", "K3", "V3"));
        Assert.assertEquals(CacheResultCode.NOT_EXISTS, cache.get(cc, "S1", "K1").getResultCode());
        Assert.assertEquals(CacheResultCode.SUCCESS, cache.get(cc, "S1", "K2").getResultCode());
        Assert.assertEquals(CacheResultCode.SUCCESS, cache.get(cc, "S1", "K3").getResultCode());
    }

    @Test
    public void testLRU2(){
        cc.setLocalLimit(2);
        Assert.assertEquals(CacheResultCode.SUCCESS, cache.put(cc, "S1", "K1", "V1"));
        Assert.assertEquals(CacheResultCode.SUCCESS, cache.put(cc, "S1", "K2", "V2"));
        Assert.assertEquals(CacheResultCode.SUCCESS, cache.get(cc, "S1", "K1").getResultCode());
        Assert.assertEquals(CacheResultCode.SUCCESS, cache.put(cc, "S1", "K3", "V3"));
        Assert.assertEquals(CacheResultCode.SUCCESS, cache.get(cc, "S1", "K1").getResultCode());
        Assert.assertEquals(CacheResultCode.NOT_EXISTS, cache.get(cc, "S1", "K2").getResultCode());
        Assert.assertEquals(CacheResultCode.SUCCESS, cache.get(cc, "S1", "K3").getResultCode());
    }

    @Test
    public void testLRU3()throws Exception{
        cc.setExpire(1);
        Assert.assertEquals(CacheResultCode.SUCCESS, cache.put(cc, "S1", "K1", "V1"));
        Assert.assertEquals(CacheResultCode.SUCCESS, cache.get(cc, "S1", "K1").getResultCode());
        Thread.sleep(1001);
        Assert.assertEquals(CacheResultCode.EXPIRED, cache.get(cc, "S1", "K1").getResultCode());
    }

    @Test
    public void testNull(){
        Assert.assertEquals(CacheResultCode.SUCCESS, cache.put(cc, "S1", "K1", null));
        Assert.assertEquals(CacheResultCode.SUCCESS, cache.get(cc, "S1", "K1").getResultCode());
        Assert.assertNull(cache.get(cc, "S1", "K1").getValue());
    }
}
