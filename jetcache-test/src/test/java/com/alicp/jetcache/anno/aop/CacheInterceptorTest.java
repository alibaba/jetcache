/**
 * Created on  13-09-23 09:27
 */
package com.alicp.jetcache.anno.aop;

import com.alicp.jetcache.anno.Cached;
import com.alicp.jetcache.anno.TestUtil;
import com.alicp.jetcache.anno.method.CacheInvokeConfig;
import com.alicp.jetcache.anno.support.CacheContext;
import com.alicp.jetcache.anno.support.ConfigProvider;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import org.aopalliance.intercept.MethodInvocation;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.*;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CacheInterceptorTest {
    private CachePointcut pc;
    private CacheInterceptor interceptor;
    private GlobalCacheConfig globalCacheConfig;

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    @Before
    public void setup() {
        globalCacheConfig = TestUtil.createGloableConfig(new ConfigProvider());
        globalCacheConfig.init();
        pc = new CachePointcut(new String[]{"com.alicp.jetcache"});
        ConcurrentHashMap<Method, CacheInvokeConfig> map = new ConcurrentHashMap();
        pc.setCacheConfigMap(map);
        interceptor = new CacheInterceptor();
        interceptor.setCacheConfigMap(map);
        interceptor.setGlobalCacheConfig(globalCacheConfig);
    }

    @After
    public void stop(){
        globalCacheConfig.shutdown();
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
        final MethodInvocation mi = context.mock(MethodInvocation.class);

        context.checking(new Expectations() {
            {
                try {
                    allowing(mi).getMethod();
                    will(Expectations.returnValue(m));
                    allowing(mi).getThis();
                    will(Expectations.returnValue(c));
                    allowing(mi).getArguments();
                    will(Expectations.returnValue(null));
                    oneOf(mi).proceed();
                } catch (Throwable e) {
                    Assert.fail();
                }
            }
        });

        interceptor.invoke(mi);
        interceptor.invoke(mi);
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
        final MethodInvocation mi = context.mock(MethodInvocation.class);

        context.checking(new Expectations() {
            {
                try {
                    allowing(mi).getMethod();
                    will(Expectations.returnValue(m));
                    allowing(mi).getThis();
                    will(Expectations.returnValue(c));
                    allowing(mi).getArguments();
                    will(Expectations.returnValue(null));
                    oneOf(mi).proceed();
                    oneOf(mi).proceed();

                    oneOf(mi).proceed();
                } catch (Throwable e) {
                    Assert.fail();
                }
            }
        });

        interceptor.invoke(mi);
        interceptor.invoke(mi);
        CacheContext.enableCache(() -> {
            try {
                interceptor.invoke(mi);
                interceptor.invoke(mi);
                return null;
            } catch (Throwable e) {
                Assert.fail();
                return null;
            }
        });
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
        final MethodInvocation mi = context.mock(MethodInvocation.class);

        context.checking(new Expectations() {
            {
                try {
                    allowing(mi).getMethod();
                    will(Expectations.returnValue(m));
                    allowing(mi).getThis();
                    will(Expectations.returnValue(c));
                    allowing(mi).getArguments();
                    will(Expectations.returnValue(null));
                    oneOf(mi).proceed();
                    will(Expectations.throwException(new SQLException()));
                } catch (Throwable e) {
                    Assert.fail();
                }
            }
        });

        try {
            interceptor.invoke(mi);
            Assert.fail();
        } catch (SQLException e) {
        }
    }
}
