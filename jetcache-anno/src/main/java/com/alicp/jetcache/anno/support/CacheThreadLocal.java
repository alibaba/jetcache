/**
 * Created on  13-09-11 17:21
 */
package com.alicp.jetcache.anno.support;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
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
