package com.alicp.jetcache;

/**
 * Created on 2016/9/28.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheResult {

    private CacheResultCode resultCode;
    private String message;

    public static final String MSG_ILLEGAL_ARGUMENT = "illegal argument";

    public static final CacheResult SUCCESS_WITHOUT_MSG = new CacheResult(CacheResultCode.SUCCESS, null);
    public static final CacheResult PART_SUCCESS_WITHOUT_MSG = new CacheResult(CacheResultCode.PART_SUCCESS, null);
    public static final CacheResult FAIL_WITHOUT_MSG = new CacheResult(CacheResultCode.FAIL, null);
    public static final CacheResult FAIL_ILLEGAL_ARGUMENT = new CacheResult(CacheResultCode.FAIL, MSG_ILLEGAL_ARGUMENT);
    public static final CacheResult EXISTS_WITHOUT_MSG = new CacheResult(CacheResultCode.EXISTS, null);


    public CacheResult(CacheResultCode resultCode, String message){
        this.resultCode = resultCode;
        this.message = message;
    }

    public CacheResult(Throwable e) {
        this.resultCode = CacheResultCode.FAIL;
        this.message = "Ex : " + e.getClass() + ", " + e.getMessage();
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
