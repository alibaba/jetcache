/**
 * Created on  13-09-23 09:27
 */
package com.alicp.jetcache.anno.aop;

import com.alicp.jetcache.anno.Cached;
import com.alicp.jetcache.anno.support.CacheContext;
import com.alicp.jetcache.anno.support.ConfigMap;
import com.alicp.jetcache.anno.support.ConfigProvider;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import com.alicp.jetcache.test.anno.TestUtil;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.sql.SQLException;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class JetCacheInterceptorTest {
    private CachePointcut pc;
    private JetCacheInterceptor interceptor;
    private GlobalCacheConfig globalCacheConfig;
    private ConfigProvider configProvider;

    @BeforeEach
    public void setup() {
        configProvider = TestUtil.createConfigProvider();
        globalCacheConfig = configProvider.getGlobalCacheConfig();
        configProvider.init();
        pc = new CachePointcut(new String[]{"com.alicp.jetcache"});
        ConfigMap map = new ConfigMap();
        pc.setCacheConfigMap(map);
        interceptor = new JetCacheInterceptor();
        interceptor.setCacheConfigMap(map);
        interceptor.configProvider = configProvider;

    }

    @AfterEach
    public void stop(){
        configProvider.shutdown();
    }

    interface I1 {
        @Cached
        int foo();
    }

    class C1 implements I1 {
        public int foo() {
            return 0;
        }
    }

    @Test
    public void test1() throws Throwable {
        final Method m = I1.class.getMethod("foo");
        final C1 c = new C1();
        pc.matches(m, C1.class);
        final MethodInvocation mi = mock(MethodInvocation.class);

        when(mi.getMethod()).thenReturn(m);
        when(mi.getThis()).thenReturn(c);
        when(mi.getArguments()).thenReturn(null);
        when(mi.proceed()).thenReturn(100);

        interceptor.invoke(mi);
        interceptor.invoke(mi);

        verify(mi, times(1)).proceed();

        globalCacheConfig.setEnableMethodCache(false);
        interceptor.invoke(mi);
        verify(mi, times(2)).proceed();
    }

    interface I2 {
        @Cached(enabled = false)
        int foo();
    }

    class C2 implements I2 {

        public int foo() {
            return 0;
        }
    }

    @Test
    public void test2() throws Throwable {
        final Method m = I2.class.getMethod("foo");
        final C2 c = new C2();
        pc.matches(m, C2.class);
        final MethodInvocation mi = mock(MethodInvocation.class);

        when(mi.getMethod()).thenReturn(m);
        when(mi.getThis()).thenReturn(c);
        when(mi.getArguments()).thenReturn(null);
        when(mi.proceed()).thenReturn(100);

        interceptor.invoke(mi);
        interceptor.invoke(mi);
        CacheContext.enableCache(() -> {
            try {
                interceptor.invoke(mi);
                interceptor.invoke(mi);
                return null;
            } catch (Throwable e) {
                fail(e);
                return null;
            }
        });

        verify(mi, times(3)).proceed();
    }

    interface I3 {
        @Cached
        int foo() throws SQLException;
    }

    class C3 implements I3 {

        public int foo() {
            return 0;
        }
    }

    @Test
    public void test3() throws Throwable {
        final Method m = I3.class.getMethod("foo");
        final C3 c = new C3();
        pc.matches(m, C3.class);
        final MethodInvocation mi = mock(MethodInvocation.class);

        when(mi.getMethod()).thenReturn(m);
        when(mi.getThis()).thenReturn(c);
        when(mi.getArguments()).thenReturn(null);
        when(mi.proceed()).thenThrow(new SQLException(""));

        try {
            interceptor.invoke(mi);
            fail("");
        } catch (SQLException e) {
        }
    }
}
