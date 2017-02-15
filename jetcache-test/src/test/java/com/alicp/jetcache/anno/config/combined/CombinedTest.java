package com.alicp.jetcache.anno.config.combined;

import com.alicp.jetcache.anno.config.AnnoConfigTest;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import com.alicp.jetcache.anno.support.SpringConfigProvider;
import com.alicp.jetcache.test.anno.TestUtil;
import com.alicp.jetcache.test.beans.MyFactoryBean;
import com.alicp.jetcache.test.spring.SpringTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created on 2017/2/14.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(classes = CombinedTest.A.class)
public class CombinedTest extends SpringTest {
    @Test
    public void test() {
        context = new ClassPathXmlApplicationContext("combined/combined.xml");
        doTest();

        Service serviceDelegate = (Service) context.getBean("combinedServiceDelegate");
        Assert.assertEquals(serviceDelegate.m1(), serviceDelegate.m1());
        Assert.assertEquals(serviceDelegate.m2(), serviceDelegate.m2());

        Service service = (Service) context.getBean("combinedService");
        Assert.assertEquals(service.m1(), service.m1());
        Assert.assertEquals(service.m2(), service.m2());
    }


    @Configuration
//    @ComponentScan({"com.alicp.jetcache.test.beans", "com.alicp.jetcache.anno.config.combined"})
    @EnableMethodCache(basePackages = {"com.alicp.jetcache.test.beans", "com.alicp.jetcache.anno.config.combined"})
    public static class A {
        @Bean
        public SpringConfigProvider springConfigProvider() {
            return new SpringConfigProvider();
        }
        @Bean
        public GlobalCacheConfig config(SpringConfigProvider configProvider) {
            GlobalCacheConfig pc = TestUtil.createGloableConfig(configProvider);
            return pc;
        }
//        @Bean(name = "factoryBeanTarget")
//        public MyFactoryBean factoryBean(){
//            return new MyFactoryBean();
//        }
    }
}
