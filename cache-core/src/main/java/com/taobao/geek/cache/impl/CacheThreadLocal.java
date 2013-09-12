/**
 * Created on  13-09-11 17:21
 */
package com.taobao.geek.cache.impl;

/**
 * @author yeli.hl
 */
class CacheThreadLocal {

    private int enabledCount = 0;

    int getEnabledCount() {
        return enabledCount;
    }

    void setEnabledCount(int enabledCount) {
        this.enabledCount = enabledCount;
    }
}
