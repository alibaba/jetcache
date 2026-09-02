package com.alicp.jetcache.support;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/11/3.
 *
 * @author huangli
 */
public class DefaultMetricsManagerTest {
    @Test
    public void testFirstResetTime() {
        LocalDateTime t = LocalDateTime.of(2016, 11, 11, 23, 50, 33, 123243242);

        LocalDateTime rt = DefaultMetricsManager.computeFirstResetTime(t, 1, TimeUnit.SECONDS);
        Assertions.assertEquals(t.withSecond(34).withNano(0), rt);
        rt = DefaultMetricsManager.computeFirstResetTime(t, 13, TimeUnit.SECONDS);
        Assertions.assertEquals(t.withSecond(34).withNano(0), rt);
        rt = DefaultMetricsManager.computeFirstResetTime(t, 30, TimeUnit.SECONDS);
        Assertions.assertEquals(t.withMinute(51).withSecond(0).withNano(0), rt);

        rt = DefaultMetricsManager.computeFirstResetTime(t, 1, TimeUnit.MINUTES);
        Assertions.assertEquals(t.withMinute(51).withSecond(0).withNano(0), rt);
        rt = DefaultMetricsManager.computeFirstResetTime(t, 7, TimeUnit.MINUTES);
        Assertions.assertEquals(t.withMinute(51).withSecond(0).withNano(0), rt);
        rt = DefaultMetricsManager.computeFirstResetTime(t, 5, TimeUnit.MINUTES);
        Assertions.assertEquals(t.withMinute(55).withSecond(0).withNano(0), rt);
        rt = DefaultMetricsManager.computeFirstResetTime(t, 15, TimeUnit.MINUTES);
        Assertions.assertEquals(t.withDayOfMonth(12).withHour(0).withMinute(0).withSecond(0).withNano(0), rt);

        rt = DefaultMetricsManager.computeFirstResetTime(t, 1, TimeUnit.HOURS);
        Assertions.assertEquals(t.withDayOfMonth(12).withHour(0).withMinute(0).withSecond(0).withNano(0), rt);
        rt = DefaultMetricsManager.computeFirstResetTime(t, 5, TimeUnit.HOURS);
        Assertions.assertEquals(t.withDayOfMonth(12).withHour(0).withMinute(0).withSecond(0).withNano(0), rt);
        rt = DefaultMetricsManager.computeFirstResetTime(t, 6, TimeUnit.HOURS);
        Assertions.assertEquals(t.withDayOfMonth(12).withHour(0).withMinute(0).withSecond(0).withNano(0), rt);

        rt = DefaultMetricsManager.computeFirstResetTime(t, 1, TimeUnit.DAYS);
        Assertions.assertEquals(t.withDayOfMonth(12).withHour(0).withMinute(0).withSecond(0).withNano(0), rt);
        rt = DefaultMetricsManager.computeFirstResetTime(t, 2, TimeUnit.DAYS);
        Assertions.assertEquals(t.withDayOfMonth(12).withHour(0).withMinute(0).withSecond(0).withNano(0), rt);

        try {
            DefaultMetricsManager.computeFirstResetTime(t, 1, TimeUnit.MILLISECONDS);
            Assertions.fail();
        } catch (Exception e) {
        }
    }
}
