/**
 * Created on  13-09-09 18:16
 */
package com.alicp.jetcache.support;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheResult<V> {
    private CacheResultCode resultCode;
    private V value;

    public CacheResult(CacheResultCode resultCode, V value) {
        this.resultCode = resultCode;
        this.value = value;
    }

    public boolean isSuccess() {
        return resultCode == CacheResultCode.SUCCESS;
    }

    public CacheResultCode getResultCode() {
        return resultCode;
    }

    public V getValue() {
        return value;
    }

    public void setResultCode(CacheResultCode resultCode) {
        this.resultCode = resultCode;
    }

    public void setValue(V value) {
        this.value = value;
    }

}
