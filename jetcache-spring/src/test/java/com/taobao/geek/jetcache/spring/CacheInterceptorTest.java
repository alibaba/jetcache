/**
 * Created on  13-09-23 09:27
 */
package com.taobao.geek.jetcache.spring;

import com.alibaba.fastjson.util.IdentityHashMap;
import com.taobao.geek.jetcache.CacheClient;
import com.taobao.geek.jetcache.CacheContext;
import com.taobao.geek.jetcache.Cached;
import com.taobao.geek.jetcache.Callback;
import com.taobao.geek.jetcache.impl.CacheAnnoConfig;
import org.aopalliance.intercept.MethodInvocation;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.*;

import java.lang.reflect.Method;

/**
 * @author yeli.hl
 */
@Ignore
public class CacheInterceptorTest {
    private CachePointcut pc;
    private IdentityHashMap<Method, CacheAnnoConfig> map;
    private CacheInterceptor interceptor;

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    @Before
    public void setup() {
        pc = new CachePointcut();
        map = new IdentityHashMap<Method, CacheAnnoConfig>();
        pc.setCacheConfigMap(map);
        interceptor = new CacheInterceptor();
        interceptor.setCacheConfigMap(map);
        interceptor.setCacheProviderFactory(TestUtil.getCacheProviderFactory());
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
        final Method m1 = I1.class.getMethod("foo");
        final C1 c1 = new C1();
        pc.matches(m1, C1.class);
        final MethodInvocation mi = context.mock(MethodInvocation.class);

        context.checking(new Expectations() {
            {
                try {
                    allowing(mi).getMethod();
                    will(returnValue(m1));
                    allowing(mi).getThis();
                    will(returnValue(c1));
                    allowing(mi).getArguments();
                    will(returnValue(null));
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
                    will(returnValue(m));
                    allowing(mi).getThis();
                    will(returnValue(c));
                    allowing(mi).getArguments();
                    will(returnValue(null));
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
        CacheContext.enableCache(new Callback() {
            @Override
            public void execute() throws Throwable {
                interceptor.invoke(mi);
                interceptor.invoke(mi);
            }
        });
    }
}
