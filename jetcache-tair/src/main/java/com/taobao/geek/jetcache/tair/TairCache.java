/**
 * Created on  13-09-14 11:12
 */
package com.taobao.geek.jetcache.tair;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.taobao.geek.jetcache.support.Cache;
import com.taobao.geek.jetcache.support.CacheConfig;
import com.taobao.geek.jetcache.support.CacheResult;
import com.taobao.geek.jetcache.support.CacheResultCode;
import com.taobao.tair.DataEntry;
import com.taobao.tair.Result;
import com.taobao.tair.ResultCode;
import com.taobao.tair.TairManager;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class TairCache implements Cache {

    private TairManager tairManager;
    private int namespace;

    @Override
    public CacheResult get(CacheConfig cacheConfig, String subArea, String key) {
        key = subArea + key;
        CacheResultCode code;
        Object value = null;
        try {
            Result<DataEntry> tairResult = tairManager.get(namespace, key);
            if (tairResult.isSuccess()) {
                DataEntry dn = tairResult.getValue();
                if (dn != null && dn.getValue() != null) {
                    byte[] bytes = (byte[]) dn.getValue();
                    TairValue tv = decode(bytes);
                    if (System.currentTimeMillis() >= tv.e) {
                        code = CacheResultCode.EXPIRED;
                    } else {
                        code = CacheResultCode.SUCCESS;
                        value = tv.v;
                    }
                } else {
                    code = CacheResultCode.NOT_EXISTS;
                }
            } else {
                int tairRc = tairResult.getRc().getCode();
                if (tairRc == ResultCode.DATANOTEXSITS.getCode()) {
                    code = CacheResultCode.NOT_EXISTS;
                } else if (tairRc == ResultCode.DATAEXPIRED.getCode()) {
                    code = CacheResultCode.EXPIRED;
                } else {
                    code = CacheResultCode.FAIL;
                }
            }
        } catch (Exception e) {
            code = CacheResultCode.FAIL;
        }
        return new CacheResult(code, value);
    }

    @Override
    public CacheResultCode put(CacheConfig cacheConfig, String subArea, String key, Object value) {
        key = subArea + key;
        try {
            TairValue tv = new TairValue();
            tv.v = value;
            tv.e = System.currentTimeMillis() + cacheConfig.getExpire() * 1000;
            byte[] bytes = encode(tv);
            ResultCode tairCode = tairManager.put(namespace, key, bytes, 0, cacheConfig.getExpire());
            if (tairCode.getCode() == ResultCode.SUCCESS.getCode()) {
                return CacheResultCode.SUCCESS;
            } else {
                return CacheResultCode.FAIL;
            }
        } catch (Exception e) {
            return CacheResultCode.FAIL;
        }
    }

    byte[] encode(TairValue value) {
        return JSON.toJSONBytes(value, SerializerFeature.WriteClassName);
    }

    TairValue decode(byte[] bytes) {
        return (TairValue) JSON.parse(bytes);
    }

    public TairManager getTairManager() {
        return tairManager;
    }

    public void setTairManager(TairManager tairManager) {
        this.tairManager = tairManager;
    }

    public int getNamespace() {
        return namespace;
    }

    public void setNamespace(int namespace) {
        this.namespace = namespace;
    }
}
