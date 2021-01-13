/**
 * Created on 2018/1/22.
 */
package com.alicp.jetcache.anno.support;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class CacheInvalidateAnnoConfig extends CacheAnnoConfig {
    private boolean multi;

    public boolean isMulti() {
        return multi;
    }

    public void setMulti(boolean multi) {
        this.multi = multi;
    }
}
