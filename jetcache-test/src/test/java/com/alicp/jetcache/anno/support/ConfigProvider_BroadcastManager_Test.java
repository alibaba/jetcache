/**
 * Created on 2019/2/2.
 */
package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.alicp.jetcache.anno.config.EnableCreateCacheAnnotation;
import com.alicp.jetcache.external.MockRemoteCacheBuilder;
import com.alicp.jetcache.support.CacheMessage;
import com.alicp.jetcache.test.anno.TestUtil;
import com.alicp.jetcache.test.spring.SpringTestBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author huangli
 */
@SpringJUnitConfig(ConfigProvider_BroadcastManager_Test.class)
@Configuration
@EnableCreateCacheAnnotation
@Import(JetCacheBaseBeans.class)
public class ConfigProvider_BroadcastManager_Test extends SpringTestBase {

    @Bean
    public GlobalCacheConfig config() {
        MockRemoteCacheBuilder.reset();
        GlobalCacheConfig pc = TestUtil.createGloableConfig();
        return pc;
    }

    public static class CountBean {
        @CreateCache(cacheType = CacheType.BOTH, syncLocal = true)
        Cache cache;
    }

    @Bean
    public CountBean countBean() {
        return new CountBean();
    }

    @Test
    public void test() {
        CountBean bean = context.getBean(CountBean.class);
        bean.cache.put("K1", "V1");
        Assertions.assertEquals(CacheMessage.TYPE_PUT, MockRemoteCacheBuilder.getLastPublishMessage().getType());
        Assertions.assertEquals("K1", MockRemoteCacheBuilder.getLastPublishMessage().getKeys()[0]);

        SortedMap<String, String> kvs = new TreeMap(Stream.of(new String[]{"K1", "V1_new"},
                                                              new String[]{"K2", "V2"})
                                                   .collect(Collectors.toMap(kv -> kv[0],
                                                                             kv -> kv[1])));
        bean.cache.putAll(kvs);
        Assertions.assertEquals(CacheMessage.TYPE_PUT_ALL, MockRemoteCacheBuilder.getLastPublishMessage().getType());
        Assertions.assertEquals("K1", MockRemoteCacheBuilder.getLastPublishMessage().getKeys()[0]);
        Assertions.assertEquals("K2", MockRemoteCacheBuilder.getLastPublishMessage().getKeys()[1]);

        bean.cache.remove("K3");
        Assertions.assertEquals(CacheMessage.TYPE_REMOVE, MockRemoteCacheBuilder.getLastPublishMessage().getType());
        Assertions.assertEquals("K3", MockRemoteCacheBuilder.getLastPublishMessage().getKeys()[0]);

        SortedSet<String> keys = new TreeSet(Stream.of("K1", "K3")
                                            .collect(Collectors.toSet()));
        bean.cache.removeAll(keys);
        Assertions.assertEquals(CacheMessage.TYPE_REMOVE_ALL, MockRemoteCacheBuilder.getLastPublishMessage().getType());
        Assertions.assertEquals("K1", MockRemoteCacheBuilder.getLastPublishMessage().getKeys()[0]);
        Assertions.assertEquals("K3", MockRemoteCacheBuilder.getLastPublishMessage().getKeys()[1]);
    }

}
