/**
 * Created on  13-09-11 17:21
 */
package com.taobao.geek.cache;

/**
 * @author yeli.hl
 */
class CacheThreadLocal {
    private boolean enabled;

    boolean isEnabled() {
        return enabled;
    }

    void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
