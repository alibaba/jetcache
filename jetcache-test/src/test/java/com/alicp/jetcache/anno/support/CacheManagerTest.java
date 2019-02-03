/**
 * Created on 2019/2/2.
 */
package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.anno.Cached;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import com.alicp.jetcache.test.anno.TestUtil;
import com.alicp.jetcache.test.spring.SpringTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.Assert;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CacheManagerTest.class)
@Configuration
@EnableMethodCache(basePackages = {"com.alicp.jetcache.anno.support.CacheManagerTest"})
public class CacheManagerTest extends SpringTestBase {

    @Bean
    public SpringConfigProvider springConfigProvider() {
        return new SpringConfigProvider();
    }

    @Bean
    public GlobalCacheConfig config(SpringConfigProvider configProvider) {
        GlobalCacheConfig pc = TestUtil.createGloableConfig(configProvider);
        return pc;
    }


    public static class CountBean {
        private int i;

        @Cached(name = "C1", expire = 3, key = "#key")
        public String count(String key) {
            return key + i++;
        }
    }

    @Bean
    public CountBean countBean() {
        return new CountBean();
    }


    @Before
    @After
    public void init() {
        SimpleCacheManager.defaultManager.rebuild();
    }

    @Test
    public void test() {
        CountBean bean = context.getBean(CountBean.class);
        String value = (bean.count("K1"));
        Assert.assertEquals(value, bean.count("K1"));
        CacheManager.defaultManager().getCache("C1").remove("K1");
        Assert.assertNotEquals(value, bean.count("K1"));
    }

}
