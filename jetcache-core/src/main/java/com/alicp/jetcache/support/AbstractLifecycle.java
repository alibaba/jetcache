/**
 * Created on 2019/6/8.
 */
package com.alicp.jetcache.support;


import java.util.concurrent.locks.ReentrantLock;

/**
 * @author huangli
 */
public class AbstractLifecycle {
    private boolean init;
    private boolean shutdown;

    final ReentrantLock reentrantLock = new ReentrantLock();

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
}
