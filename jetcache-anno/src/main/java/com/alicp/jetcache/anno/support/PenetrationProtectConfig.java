/**
 * Created on 2018/4/27.
 */
package com.alicp.jetcache.anno.support;

import java.time.Duration;

/**
 * @author huangli
 */
public class PenetrationProtectConfig {
    private boolean penetrationProtect;
    private Duration penetrationProtectTimeout;

    public boolean isPenetrationProtect() {
        return penetrationProtect;
    }

    public void setPenetrationProtect(boolean penetrationProtect) {
        this.penetrationProtect = penetrationProtect;
    }

    public Duration getPenetrationProtectTimeout() {
        return penetrationProtectTimeout;
    }

    public void setPenetrationProtectTimeout(Duration penetrationProtectTimeout) {
        this.penetrationProtectTimeout = penetrationProtectTimeout;
    }
}
