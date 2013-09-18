/**
 * Created on  13-09-14 11:12
 */
package com.taobao.geek.jetcache.tair;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.taobao.geek.jetcache.Cache;
import com.taobao.geek.jetcache.CacheConfig;
import com.taobao.geek.jetcache.CacheResult;
import com.taobao.geek.jetcache.CacheResultCode;
import com.taobao.tair.DataEntry;
import com.taobao.tair.Result;
import com.taobao.tair.ResultCode;
import com.taobao.tair.TairManager;

/**
 * @author yeli.hl
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
                    code = CacheResultCode.SUCCESS;
                    byte[] bytes = (byte[])dn.getValue();
                    value = JSON.parse(bytes);
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
            byte[] bytes = JSON.toJSONBytes(value, SerializerFeature.WriteClassName);
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
