/**
 * Created on  13-09-24 18:47
 */
package com.taobao.geek.jetcache.tair;

import com.taobao.geek.jetcache.impl.CacheImplSupport;
import com.taobao.geek.jetcache.support.CacheConfig;
import com.taobao.geek.jetcache.support.CacheResult;
import com.taobao.geek.jetcache.support.CacheResultCode;
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
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class TairCacheTest {

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();
    private TairCache cache;
    private TairManager tairManager;
    private CacheConfig cc;

    @Before
    public void setup() {
        tairManager = context.mock(TairManager.class);
        cache = new TairCache();
        cache.setNamespace(20);
        cache.setTairManager(tairManager);
        cc = new CacheConfig();
    }

    @Test
    public void testGet() {
        context.checking(new Expectations() {
            {
                try {
                    oneOf(tairManager).get(20, "SA1K1");
                    will(returnValue(new Result(ResultCode.DATANOTEXSITS)));
                    oneOf(tairManager).get(20, "SA2K2");
                    will(returnValue(new Result(ResultCode.DATAEXPIRED)));
                    oneOf(tairManager).get(20, "SA3K3");
                    will(returnValue(new Result(ResultCode.PARTSUCC)));
                    oneOf(tairManager).get(20, "SA4K4");
                    will(throwException(new RuntimeException()));
                    oneOf(tairManager).get(20, "SA5K5");
                    byte[] bs = CacheImplSupport.encodeValue(new TairValue(System.currentTimeMillis() + 100000, "V"),
                            cc.getSerialPolicy());
                    DataEntry dn = new DataEntry(bs);
                    will(returnValue(new Result<DataEntry>(ResultCode.SUCCESS, dn)));
                    oneOf(tairManager).get(20, "SA6K6");
                    bs = CacheImplSupport.encodeValue(new TairValue(System.currentTimeMillis() - 1, "V"),
                            cc.getSerialPolicy());
                    DataEntry dn2 = new DataEntry(bs);
                    will(returnValue(new Result<DataEntry>(ResultCode.SUCCESS, dn2)));
                } catch (Exception e) {
                    Assert.fail();
                }
            }
        });
        Assert.assertEquals(CacheResultCode.NOT_EXISTS, cache.get(cc, "SA1", "K1").getResultCode());
        Assert.assertEquals(CacheResultCode.EXPIRED, cache.get(cc, "SA2", "K2").getResultCode());
        Assert.assertEquals(CacheResultCode.FAIL, cache.get(cc, "SA3", "K3").getResultCode());
        Assert.assertEquals(CacheResultCode.FAIL, cache.get(cc, "SA4", "K4").getResultCode());
        CacheResult result = cache.get(null, "SA5", "K5");
        Assert.assertEquals(CacheResultCode.SUCCESS, result.getResultCode());
        Assert.assertEquals("V", result.getValue());

        Assert.assertEquals(CacheResultCode.EXPIRED, cache.get(cc, "SA6", "K6").getResultCode());
    }

    @Test
    public void testPut() {
        final CacheConfig cc = new CacheConfig();
        context.checking(new Expectations() {
            {
                oneOf(tairManager).put(with(20), with("SA1K1"), with(any(byte[].class)), with(0), with(cc.getExpire()));
                will(returnValue(ResultCode.SUCCESS));
                oneOf(tairManager).put(with(20), with("SA2K2"), with(any(byte[].class)), with(0), with(cc.getExpire()));
                will(returnValue(ResultCode.CONNERROR));
            }
        });
        Assert.assertEquals(CacheResultCode.SUCCESS, cache.put(cc, "SA1", "K1", "V1"));
        Assert.assertEquals(CacheResultCode.FAIL, cache.put(cc, "SA2", "K2", "V2"));
    }

    @Test
    public void testNull() {
        final CacheConfig cc = new CacheConfig();
        context.checking(new Expectations() {
            {
                try {
                    oneOf(tairManager).put(with(20), with("SA1K1"), with(any(byte[].class)), with(0), with(cc.getExpire()));
                    will(returnValue(ResultCode.SUCCESS));
                    oneOf(tairManager).get(20, "SA1K1");
                    byte[] bs = CacheImplSupport.encodeValue(new TairValue(System.currentTimeMillis() + 100000, null),
                            cc.getSerialPolicy());
                    DataEntry dn = new DataEntry(bs);
                    will(returnValue(new Result<DataEntry>(ResultCode.SUCCESS, dn)));
                } catch (Exception e) {
                    Assert.fail();
                }
            }
        });
        Assert.assertEquals(CacheResultCode.SUCCESS, cache.put(cc, "SA1", "K1", null));
        CacheResult cr = cache.get(cc, "SA1", "K1");
        Assert.assertEquals(CacheResultCode.SUCCESS, cr.getResultCode());
        Assert.assertNull(cr.getValue());
    }

}
