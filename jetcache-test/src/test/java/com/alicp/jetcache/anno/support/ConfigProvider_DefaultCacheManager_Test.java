/**
 * Created on 2019/2/2.
 */
package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.anno.Cached;
import com.alicp.jetcache.anno.CreateCache;
import com.alicp.jetcache.anno.config.EnableCreateCacheAnnotation;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import com.alicp.jetcache.test.anno.TestUtil;
import com.alicp.jetcache.test.spring.SpringTestBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * @author huangli
 */
@SpringJUnitConfig(ConfigProvider_DefaultCacheManager_Test.class)
@Configuration
@EnableMethodCache(basePackages = {"com.alicp.jetcache.anno.support.ConfigProvider_DefaultCacheManager_Test"})
@EnableCreateCacheAnnotation
@Import(JetCacheBaseBeans.class)
public class ConfigProvider_DefaultCacheManager_Test extends SpringTestBase {

    @Bean
    public GlobalCacheConfig config() {
        GlobalCacheConfig pc = TestUtil.createGloableConfig();
        return pc;
    }

    public static class CountBean {
        private int i;

        @CreateCache(name = "C2")
        private Cache c2;

        @Cached(name = "C1", expire = 3, key = "#key")
        public String count(String key) {
            return key + i++;
        }
    }

    @Bean
    public CountBean countBean() {
        return new CountBean();
    }

    @Test
    public void test() {
        CountBean bean = context.getBean(CountBean.class);
        String value = (bean.count("K1"));
        Assertions.assertEquals(value, bean.count("K1"));
        CacheManager cm = context.getBean(CacheManager.class);
        cm.getCache("C1").remove("K1");
        Assertions.assertNotEquals(value, bean.count("K1"));
    }

    @Test
    public void test2() {
        CacheManager cm = context.getBean(CacheManager.class);
        Assertions.assertNotNull(cm.getCache("C1"));
        Assertions.assertNotNull(cm.getCache("C2"));
    }

}
