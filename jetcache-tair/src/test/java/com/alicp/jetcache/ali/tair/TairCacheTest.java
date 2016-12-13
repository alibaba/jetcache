/**
 * Created on  13-09-24 18:47
 */
package com.alicp.jetcache.ali.tair;

import com.alicp.jetcache.CacheGetResult;
import com.alicp.jetcache.CacheResultCode;
import com.alicp.jetcache.CacheValueHolder;
import com.alicp.jetcache.anno.CacheConsts;
import com.alicp.jetcache.anno.method.SerializeUtil;
import com.taobao.tair.DataEntry;
import com.taobao.tair.Result;
import com.taobao.tair.ResultCode;
import com.taobao.tair.TairManager;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.*;

import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
@Ignore
public class TairCacheTest {

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();
    private TairCache cache;
    private TairManager tairManager;
    private static final String sp = CacheConsts.DEFAULT_SERIAL_POLICY;

    @Before
    public void setup() {
        tairManager = context.mock(TairManager.class);
        cache = (TairCache) TairCacheBuilder.createTairCacheBuilder().keyPrefix("SA").namespace(20).tairManager(tairManager).buildCache();
    }

    @Test
    public void testGet() {

        context.checking(new Expectations() {
            {
                try {
                    oneOf(tairManager).get(20, "SAK1");
                    will(returnValue(new Result(ResultCode.DATANOTEXSITS)));
                    oneOf(tairManager).get(20, "SAK2");
                    will(returnValue(new Result(ResultCode.DATAEXPIRED)));
                    oneOf(tairManager).get(20, "SAK3");
                    will(returnValue(new Result(ResultCode.PARTSUCC)));
                    oneOf(tairManager).get(20, "SAK4");
                    will(throwException(new RuntimeException()));
                    oneOf(tairManager).get(20, "SAK5");
                    byte[] bs = SerializeUtil.encode(new CacheValueHolder("V5", System.currentTimeMillis(), 100000), sp);
                    DataEntry dn = new DataEntry(bs);
                    will(returnValue(new Result(ResultCode.SUCCESS, dn)));
                    oneOf(tairManager).get(20, "SAK6");
                    bs = SerializeUtil.encode(new CacheValueHolder("V6", System.currentTimeMillis(), -1), sp);
                    DataEntry dn2 = new DataEntry(bs);
                    will(returnValue(new Result(ResultCode.SUCCESS, dn2)));
                } catch (Exception e) {
                    Assert.fail();
                }
            }
        });
        Assert.assertEquals(CacheResultCode.NOT_EXISTS, cache.GET("K1").getResultCode());
        Assert.assertEquals(CacheResultCode.EXPIRED, cache.GET("K2").getResultCode());
        Assert.assertEquals(CacheResultCode.FAIL, cache.GET("K3").getResultCode());
        Assert.assertEquals(CacheResultCode.FAIL, cache.GET("K4").getResultCode());
        CacheGetResult result = cache.GET("K5");
        Assert.assertEquals(CacheResultCode.SUCCESS, result.getResultCode());
        Assert.assertEquals("V5", result.getValue());
        result = cache.GET("K6");
        Assert.assertEquals(CacheResultCode.EXPIRED, result.getResultCode());
        Assert.assertNull(result.getValue());
    }

    @Test
    public void testPut() {
        final int expireTime = 1;
        context.checking(new Expectations() {
            {
                oneOf(tairManager).put(with(20), with("SAK1"), with(any(byte[].class)), with(0), with(expireTime));
                will(returnValue(ResultCode.SUCCESS));
                oneOf(tairManager).put(with(20), with("SAK2"), with(any(byte[].class)), with(0), with(expireTime));
                will(returnValue(ResultCode.CONNERROR));
            }
        });
        Assert.assertEquals(CacheResultCode.SUCCESS, cache.PUT("K1", "V1", expireTime, TimeUnit.SECONDS));
        Assert.assertEquals(CacheResultCode.FAIL, cache.PUT("K2", "V2", expireTime, TimeUnit.SECONDS));
    }

    @Test
    public void testNull() {
        final int expireTime = 1;
        context.checking(new Expectations() {
            {
                try {
                    oneOf(tairManager).put(with(20), with("SAK1"), with(any(byte[].class)), with(0), with(expireTime));
                    will(returnValue(ResultCode.SUCCESS));
                    oneOf(tairManager).get(20, "SAK1");
                    byte[] bs = SerializeUtil.encode(new CacheValueHolder(null, System.currentTimeMillis(), 1000), sp);
                    DataEntry dn = new DataEntry(bs);
                    will(returnValue(new Result(ResultCode.SUCCESS, dn)));
                } catch (Exception e) {
                    Assert.fail();
                }
            }
        });
        Assert.assertEquals(CacheResultCode.SUCCESS, cache.PUT("K1", null, expireTime, TimeUnit.SECONDS));
        CacheGetResult cr = cache.GET("K1");
        Assert.assertEquals(CacheResultCode.SUCCESS, cr.getResultCode());
        Assert.assertNull(cr.getValue());
    }

}
