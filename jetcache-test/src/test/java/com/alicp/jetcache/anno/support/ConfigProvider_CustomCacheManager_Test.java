/**
 * Created on 2019/2/2.
 */
package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.SimpleCacheManager;
import com.alicp.jetcache.anno.Cached;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import com.alicp.jetcache.test.anno.TestUtil;
import com.alicp.jetcache.test.spring.SpringTestBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * @author huangli
 */
@SpringJUnitConfig(ConfigProvider_CustomCacheManager_Test.class)
@Configuration
@EnableMethodCache(basePackages = {"com.alicp.jetcache.anno.support.ConfigProvider_CustomCacheManager_Test"})
public class ConfigProvider_CustomCacheManager_Test extends SpringTestBase {

    @Bean
    public GlobalCacheConfig config() {
        GlobalCacheConfig pc = TestUtil.createGloableConfig();
        return pc;
    }

    @Bean
    public SpringConfigProvider springConfigProvider(
            @Autowired ApplicationContext context,
            @Autowired GlobalCacheConfig config) {
        return new JetCacheBaseBeans().springConfigProvider(context, config, null, null, null);
    }

    @Bean
    public SimpleCacheManager cacheManager(@Autowired ConfigProvider configProvider) {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCacheBuilderTemplate(configProvider.getCacheBuilderTemplate());
        return cacheManager;
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

    @Test
    public void test() {
        CountBean bean = context.getBean(CountBean.class);
        String value = (bean.count("K1"));
        Assertions.assertEquals(value, bean.count("K1"));

        context.getBean(CacheManager.class).getCache("C1").remove("K1");
        Assertions.assertNotEquals(value, bean.count("K1"));
    }

}
