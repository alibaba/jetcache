/**
 * Created on 2019/2/2.
 */
package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CreateCache;
import com.alicp.jetcache.anno.config.EnableCreateCacheAnnotation;
import com.alicp.jetcache.support.CacheMessage;
import com.alicp.jetcache.support.CacheMessagePublisher;
import com.alicp.jetcache.test.anno.TestUtil;
import com.alicp.jetcache.test.spring.SpringTestBase;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ConfigProvider_CacheMessagePublisher_Test.class)
@Configuration
@EnableCreateCacheAnnotation
public class ConfigProvider_CacheMessagePublisher_Test extends SpringTestBase {

    @Bean
    public SimpleCacheManager cacheManager() {
        return new SimpleCacheManager();
    }

    @Bean
    public SpringConfigProvider springConfigProvider() {
        return new SpringConfigProvider();
    }

    @Bean
    public GlobalCacheConfig config() {
        GlobalCacheConfig pc = TestUtil.createGloableConfig();
        return pc;
    }

    @Component
    public static class MyCacheMessagePublisher implements CacheMessagePublisher {
        int messageType;
        Object[] keys;

        @Override
        public void publish(String area, String cacheName, CacheMessage cacheMessage) {
            messageType = cacheMessage.getType();
            keys = cacheMessage.getKeys();
        }
    }

    public static class CountBean {
        @CreateCache
        Cache cache;
    }

    @Bean
    public CountBean countBean() {
        return new CountBean();
    }

    @Test
    public void test() {
        CountBean bean = context.getBean(CountBean.class);
        MyCacheMessagePublisher publisher = context.getBean(MyCacheMessagePublisher.class);
        bean.cache.put("K1", "V1");
        Assert.assertEquals(CacheMessage.TYPE_PUT, publisher.messageType);
        Assert.assertEquals("K1", publisher.keys[0]);

        Map kvs = Stream.of(new String[]{"K1", "V1_new"}, new String[]{"K2", "V2"})
                .collect(Collectors.toMap(kv -> kv[0], kv -> kv[1]));
        bean.cache.putAll(kvs);
        Assert.assertEquals(CacheMessage.TYPE_PUT_ALL, publisher.messageType);
        Assert.assertEquals("K1", publisher.keys[0]);
        Assert.assertEquals("K2", publisher.keys[1]);

        bean.cache.remove("K3");
        Assert.assertEquals(CacheMessage.TYPE_REMOVE, publisher.messageType);
        Assert.assertEquals("K3", publisher.keys[0]);

        Set keys = Stream.of("K1", "K3").collect(Collectors.toSet());
        bean.cache.removeAll(keys);
        Assert.assertEquals(CacheMessage.TYPE_REMOVE_ALL, publisher.messageType);
        Assert.assertEquals("K1", publisher.keys[0]);
        Assert.assertEquals("K3", publisher.keys[1]);
    }

}
