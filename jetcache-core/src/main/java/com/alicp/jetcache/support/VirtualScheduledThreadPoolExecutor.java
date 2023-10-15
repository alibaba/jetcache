package com.alicp.jetcache.support;

import java.util.concurrent.*;

/**
 * @Description
 * @author: zhangtong
 * @create: 2023/10/15 12:51 PM
 */
public class VirtualScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor {

    final ExecutorService executorService;
    public VirtualScheduledThreadPoolExecutor(String factoyName) {
        super(1);
        executorService = VirtualThreadUtil.createExecuteor(factoyName);
    }

    @Override
    public void execute(Runnable runnable){
        super.execute(() -> {
            executorService.execute(runnable);
        });
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, java.util.concurrent.TimeUnit unit) {
        return super.scheduleAtFixedRate(() -> {
            executorService.execute(command);
        }, initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return super.schedule(() -> {
            executorService.execute(command);
        }, delay, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return super.scheduleWithFixedDelay(() -> {
            executorService.execute(command);
        }, initialDelay, delay, unit);
    }
}
