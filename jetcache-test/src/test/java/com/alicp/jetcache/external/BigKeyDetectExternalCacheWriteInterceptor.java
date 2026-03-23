package com.alicp.jetcache.external;

/**
 * @author youjie_li
 */
public class BigKeyDetectExternalCacheWriteInterceptor implements ExternalCacheWriteInterceptor{

    private int bigKeyCount = 0;

    @Override
    public void intercept(WriteContext ctx) {
        if (ctx.getValueSize() > 20) {
            bigKeyCount++;
        }
    }

    public int getBigKeyCount() {
        return bigKeyCount;
    }
}
