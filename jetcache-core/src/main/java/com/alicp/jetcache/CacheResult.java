package com.alicp.jetcache;

/**
 * Created on 2016/9/28.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheResult {

    private CacheResultCode resultCode;
    private String message;

    public static final CacheResult SUCCESS_WITHOUT_MSG = new CacheResult(CacheResultCode.SUCCESS, null);
    public static final CacheResult FAIL_WITHOUT_MSG = new CacheResult(CacheResultCode.FAIL, null);

    public CacheResult(CacheResultCode resultCode, String message){
        this.resultCode = resultCode;
        this.message = message;
    }

    public boolean isSuccess() {
        return resultCode == CacheResultCode.SUCCESS;
    }

    public CacheResultCode getResultCode() {
        return resultCode;
    }

    public void setResultCode(CacheResultCode resultCode) {
        this.resultCode = resultCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
