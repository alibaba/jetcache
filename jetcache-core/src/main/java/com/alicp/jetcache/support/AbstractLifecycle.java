/**
 * Created on 2019/6/8.
 */
package com.alicp.jetcache.support;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class AbstractLifecycle {
    private boolean init;
    private boolean shutdown;

    @PostConstruct
    public final synchronized void init() {
        if (!init) {
            doInit();
            init = true;
        }
    }

    protected void doInit() {
    }

    @PreDestroy
    public final synchronized void shutdown() {
        if (init && !shutdown) {
            doShutdown();
            init = false;
            shutdown = true;
        }
    }

    protected void doShutdown() {
    }
}
