/**
 * Created on 2018/1/22.
 */
package com.alicp.jetcache.anno.support;

/**
 * @author huangli
 */
public class CacheInvalidateAnnoConfig extends CacheAnnoConfig {
    private boolean multi;

    public boolean isMulti() {
        return multi;
    }

    public void setMulti(boolean multi) {
        this.multi = multi;
    }

    private boolean delByPrefix;

    public boolean isDelByPrefix() {
        return delByPrefix;
    }

    public void setDelByPrefix(boolean delByPrefix) {
        this.delByPrefix = delByPrefix;
    }
}
