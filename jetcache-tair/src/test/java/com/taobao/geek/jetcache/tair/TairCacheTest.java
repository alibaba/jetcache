/**
 * Created on  13-09-24 18:47
 */
package com.taobao.geek.jetcache.tair;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.taobao.geek.jetcache.CacheConfig;
import com.taobao.geek.jetcache.CacheResult;
import com.taobao.geek.jetcache.CacheResultCode;
import com.taobao.tair.DataEntry;
import com.taobao.tair.Result;
import com.taobao.tair.ResultCode;
import com.taobao.tair.TairManager;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author yeli.hl
 */
public class TairCacheTest {

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();
    private TairCache cache;
    private TairManager tairManager;

    @Before
    public void setup() {
        tairManager = context.mock(TairManager.class);
        cache = new TairCache();
        cache.setNamespace(20);
        cache.setTairManager(tairManager);
    }

    @Test
    public void testGet() {
        context.checking(new Expectations() {
            {
                oneOf(tairManager).get(20, "SA1K1");
                will(returnValue(new Result(ResultCode.DATANOTEXSITS)));
                oneOf(tairManager).get(20, "SA2K2");
                will(returnValue(new Result(ResultCode.DATAEXPIRED)));
                oneOf(tairManager).get(20, "SA3K3");
                will(returnValue(new Result(ResultCode.PARTSUCC)));
                oneOf(tairManager).get(20, "SA4K4");
                will(throwException(new RuntimeException()));
                oneOf(tairManager).get(20, "SA5K5");
                will(returnValue(new Result<DataEntry>(ResultCode.SUCCESS, new DataEntry(cache.encode("V")))));
            }
        });
        Assert.assertEquals(CacheResultCode.NOT_EXISTS, cache.get(null, "SA1", "K1").getResultCode());
        Assert.assertEquals(CacheResultCode.EXPIRED, cache.get(null, "SA2", "K2").getResultCode());
        Assert.assertEquals(CacheResultCode.FAIL, cache.get(null, "SA3", "K3").getResultCode());
        Assert.assertEquals(CacheResultCode.FAIL, cache.get(null, "SA4", "K4").getResultCode());
        CacheResult result = cache.get(null, "SA5", "K5");
        Assert.assertEquals(CacheResultCode.SUCCESS, result.getResultCode());
        Assert.assertEquals("V", result.getValue());
    }

    @Test
    public void testPut() {
        final CacheConfig cc = new CacheConfig();
        cc.setExpire(200);
        context.checking(new Expectations() {
            {
                oneOf(tairManager).put(20, "SA1K1", cache.encode("V1"), 0, cc.getExpire());
                will(returnValue(ResultCode.SUCCESS));
                oneOf(tairManager).put(20, "SA2K2", cache.encode("V2"), 0, cc.getExpire());
                will(returnValue(ResultCode.CONNERROR));
            }
        });
        Assert.assertEquals(CacheResultCode.SUCCESS, cache.put(cc, "SA1", "K1", "V1"));
        Assert.assertEquals(CacheResultCode.FAIL, cache.put(cc, "SA2", "K2", "V2"));
    }
}
