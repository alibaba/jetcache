/**
 * Created on 2022/7/6.
 */
package com.alicp.jetcache.support;

import org.slf4j.Logger;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author huangli
 */
public class SquashedLogger {
    private static final int DEFAULT_INTERVAL_SECONDS = 10;

    private static final ConcurrentHashMap<Logger, SquashedLogger> MAP = new ConcurrentHashMap<>();
    private final Logger logger;
    private final long interval;

    private final AtomicLong lastLogTime;

    private SquashedLogger(Logger logger, int intervalSeconds) {
        this.logger = logger;
        this.interval = Duration.ofSeconds(intervalSeconds).toNanos();
        this.lastLogTime = new AtomicLong(System.nanoTime() - interval);
    }

    public static SquashedLogger getLogger(Logger target, int intervalSeconds) {
        SquashedLogger result = MAP.get(target);
        if (result == null) {
            result = MAP.computeIfAbsent(target, k -> new SquashedLogger(k, intervalSeconds));
        }
        return result;
    }

    public static SquashedLogger getLogger(Logger target) {
        return getLogger(target, DEFAULT_INTERVAL_SECONDS);
    }

    private boolean shouldLogEx() {
        long now = System.nanoTime();
        long last = lastLogTime.get();
        if (Math.abs(now - last) >= interval) {
            return lastLogTime.compareAndSet(last, now);
        } else {
            return false;
        }
    }

    public void error(CharSequence msg, Throwable e) {
        if (shouldLogEx()) {
            logger.error(msg.toString(), e);
        } else {
            StringBuilder sb;
            if (msg instanceof StringBuilder) {
                sb = (StringBuilder) msg;
            } else {
                sb = new StringBuilder(msg.length() + 256);
                sb.append(msg);
            }
            sb.append(' ');
            int i = 0;
            while (e != null && i++ < 20) {
                sb.append(e);
                e = e.getCause();
                if (e != null) {
                    sb.append("\ncause by ");
                }
            }
            logger.error(sb.toString());
        }
    }
}
