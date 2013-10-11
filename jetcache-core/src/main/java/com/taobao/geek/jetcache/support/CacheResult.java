/**
 * Created on  13-09-09 18:16
 */
package com.taobao.geek.jetcache.support;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheResult {
    private CacheResultCode resultCode;
    private Object value;

    public CacheResult(CacheResultCode resultCode, Object value) {
        this.resultCode = resultCode;
        this.value = value;
    }

    public boolean isSuccess() {
        return resultCode == CacheResultCode.SUCCESS;
    }

    public CacheResultCode getResultCode() {
        return resultCode;
    }

    public Object getValue() {
        return value;
    }

    public void setResultCode(CacheResultCode resultCode) {
        this.resultCode = resultCode;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
