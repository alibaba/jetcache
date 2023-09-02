/**
 * Created on 2019/6/8.
 */
package com.alicp.jetcache.support;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;


import java.util.concurrent.locks.ReentrantLock;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class AbstractLifecycle implements InitializingBean, DisposableBean {
    private boolean init;
    private boolean shutdown;
    private final ReentrantLock reentrantLock = new ReentrantLock();

    public final void init() {
        reentrantLock.lock();
        try {
            if (!init) {
                doInit();
                init = true;
            }
        }finally {
            reentrantLock.unlock();
        }
    }

    protected void doInit() {
    }


    public final void shutdown() {
        reentrantLock.lock();
        try {
            if (init && !shutdown) {
                doShutdown();
                init = false;
                shutdown = true;
            }
        }finally {
            reentrantLock.unlock();
        }
    }

    protected void doShutdown() {
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.init();
    }

    @Override
    public void destroy() throws Exception {
        this.destroy();
    }
}
