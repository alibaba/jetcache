package com.alicp.jetcache.anno.config.combined;

import com.alicp.jetcache.anno.config.EnableMethodCache;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import com.alicp.jetcache.anno.support.JetCacheBaseBeans;
import com.alicp.jetcache.test.anno.TestUtil;
import com.alicp.jetcache.test.spring.SpringTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created on 2017/2/14.
 *
 * @author huangli
 */
public class CombinedTest extends SpringTest {
    @Test
    public void test() throws Exception {
//        context = new ClassPathXmlApplicationContext("combined/combined.xml", "combined/combined-aop1.xml", "combined/combined-aop2.xml");
//        testImpl();
        context = new ClassPathXmlApplicationContext("combined/combined.xml", "combined/combined-aop1.xml", "combined/combined-aop3.xml");
        testImpl();
    }

    private void testImpl() throws Exception {
        doTest();

        Service serviceDelegate = (Service) context.getBean("combinedServiceDelegate");
        Assertions.assertEquals(serviceDelegate.combinedTest1(), serviceDelegate.combinedTest1());
        Assertions.assertEquals(serviceDelegate.combinedTest2(), serviceDelegate.combinedTest2());

        Service service = (Service) context.getBean("combinedService");
        Assertions.assertEquals(service.combinedTest1(), service.combinedTest1());
        Assertions.assertEquals(service.combinedTest2(), service.combinedTest2());
    }


    @Configuration
    @EnableMethodCache(basePackages = {"com.alicp.jetcache.test.beans", "com.alicp.jetcache.anno.config.combined"})
    @Import(JetCacheBaseBeans.class)
    public static class A {

        @Bean
        public GlobalCacheConfig config() {
            GlobalCacheConfig pc = TestUtil.createGloableConfig();
            return pc;
        }
    }
}
