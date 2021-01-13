/**
 * Created on 2019/6/8.
 */
package com.alicp.jetcache.anno.support;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
class AbstractLifecycle {
    private boolean inited;

    @PostConstruct
    public final synchronized void init() {
        if (!inited) {
            doInit();
            inited = true;
        }
    }

    protected void doInit() {
    }

    @PreDestroy
    public final synchronized void shutdown() {
        if (inited) {
            doShutdown();
            inited = false;
        }
    }

    protected void doShutdown() {
    }
}
