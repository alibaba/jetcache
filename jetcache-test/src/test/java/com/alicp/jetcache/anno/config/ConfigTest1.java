package com.alicp.jetcache.anno.config;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheConsts;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.alicp.jetcache.anno.KeyConvertor;
import com.alicp.jetcache.anno.SerialPolicy;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import com.alicp.jetcache.anno.support.JetCacheBaseBeans;
import com.alicp.jetcache.embedded.EmbeddedCacheBuilder;
import com.alicp.jetcache.embedded.EmbeddedCacheConfig;
import com.alicp.jetcache.embedded.LinkedHashMapCacheBuilder;
import com.alicp.jetcache.external.ExternalCacheConfig;
import com.alicp.jetcache.external.MockRemoteCacheBuilder;
import com.alicp.jetcache.support.Fastjson2KeyConvertor;
import com.alicp.jetcache.support.JavaValueDecoder;
import com.alicp.jetcache.support.JavaValueEncoder;
import com.alicp.jetcache.support.KryoValueDecoder;
import com.alicp.jetcache.support.KryoValueEncoder;
import com.alicp.jetcache.test.beans.MyFactoryBean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/12/29.
 *
 * @author huangli
 */
@SpringJUnitConfig(ConfigTest1.A.class)
public class ConfigTest1 implements ApplicationContextAware {

    private ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    @Test
    public void test() {
        ConfigTestBean bean = context.getBean(ConfigTestBean.class);

        {
            ExternalCacheConfig c = (ExternalCacheConfig) bean.defualtRemote.config();
            Assertions.assertSame(Fastjson2KeyConvertor.INSTANCE, c.getKeyConvertor());
            Assertions.assertSame(JavaValueEncoder.INSTANCE, c.getValueEncoder());
            Assertions.assertSame(JavaValueDecoder.INSTANCE, c.getValueDecoder());
            Assertions.assertFalse(c.isExpireAfterAccess());
            Assertions.assertEquals(90, c.getExpireAfterWriteInMillis());

            c = (ExternalCacheConfig) bean.a1Remote.config();
            Assertions.assertEquals(CacheConsts.DEFAULT_EXPIRE * 1000L, c.getExpireAfterWriteInMillis());
            Assertions.assertEquals(110, c.getExpireAfterAccessInMillis());

            c = (ExternalCacheConfig) bean.customRemote.config();
            Assertions.assertFalse(c.isExpireAfterAccess());
            Assertions.assertEquals(1000, c.getExpireAfterWriteInMillis());
            Assertions.assertEquals(KryoValueEncoder.class, c.getValueEncoder().getClass());
            Assertions.assertEquals(KryoValueDecoder.class, c.getValueDecoder().getClass());
            Assertions.assertSame(Fastjson2KeyConvertor.INSTANCE, c.getKeyConvertor());
        }

        {
            EmbeddedCacheConfig c = (EmbeddedCacheConfig) bean.defaultLocal.config();
            Assertions.assertSame(Fastjson2KeyConvertor.INSTANCE, c.getKeyConvertor());
            Assertions.assertEquals(20, c.getLimit());
            Assertions.assertFalse(c.isExpireAfterAccess());
            Assertions.assertEquals(50, c.getExpireAfterWriteInMillis());

            c = (EmbeddedCacheConfig) bean.a1Local.config();
            Assertions.assertEquals(CacheConsts.DEFAULT_EXPIRE * 1000L, c.getExpireAfterWriteInMillis());
            Assertions.assertEquals(60, c.getExpireAfterAccessInMillis());

            c = (EmbeddedCacheConfig) bean.customLocal.config();
            Assertions.assertFalse(c.isExpireAfterAccess());
            Assertions.assertEquals(1000, c.getExpireAfterWriteInMillis());
            Assertions.assertEquals(123, c.getLimit());
            Assertions.assertSame(Fastjson2KeyConvertor.INSTANCE, c.getKeyConvertor());
        }
    }


    @Configuration
    @EnableMethodCache(basePackages = "com.alicp.jetcache.anno.config.ConfigTest1")
    @EnableCreateCacheAnnotation
    @Import(JetCacheBaseBeans.class)
    public static class A {
        @Bean
        public ConfigTestBean configTestBean() {
            return new ConfigTestBean();
        }

        @Bean(name = "factoryBeanTarget")
        public MyFactoryBean factoryBean() {
            return new MyFactoryBean();
        }

        @Bean
        public GlobalCacheConfig config() {
            Map localFactories = new HashMap();
            EmbeddedCacheBuilder localFactory = LinkedHashMapCacheBuilder.createLinkedHashMapCacheBuilder()
                    .limit(20).keyConvertor(Fastjson2KeyConvertor.INSTANCE).expireAfterWrite(50, TimeUnit.MILLISECONDS);
            EmbeddedCacheBuilder localFactory2 = LinkedHashMapCacheBuilder.createLinkedHashMapCacheBuilder()
                    .limit(10).keyConvertor(Fastjson2KeyConvertor.INSTANCE).expireAfterAccess(60, TimeUnit.MILLISECONDS);
            localFactories.put(CacheConsts.DEFAULT_AREA, localFactory);
            localFactories.put("A1", localFactory2);


            Map remoteFactories = new HashMap();

            MockRemoteCacheBuilder remoteBuilder = new MockRemoteCacheBuilder();
            remoteBuilder.setKeyConvertor(null);
            remoteBuilder.setKeyConvertor(Fastjson2KeyConvertor.INSTANCE);
            remoteBuilder.setValueEncoder(JavaValueEncoder.INSTANCE);
            remoteBuilder.setValueDecoder(JavaValueDecoder.INSTANCE);
            remoteBuilder.setExpireAfterWriteInMillis(90);
            remoteBuilder.keyPrefix(null);
            remoteFactories.put(CacheConsts.DEFAULT_AREA, remoteBuilder);

            remoteBuilder = new MockRemoteCacheBuilder();
            remoteBuilder.setKeyConvertor(Fastjson2KeyConvertor.INSTANCE);
            remoteBuilder.setValueEncoder(KryoValueEncoder.INSTANCE);
            remoteBuilder.setValueDecoder(KryoValueDecoder.INSTANCE);
            remoteBuilder.setExpireAfterAccessInMillis(110);
            remoteFactories.put("A1", remoteBuilder);

            GlobalCacheConfig globalCacheConfig = new GlobalCacheConfig();
//            globalCacheConfig.setConfigProvider(configProvider);
            globalCacheConfig.setLocalCacheBuilders(localFactories);
            globalCacheConfig.setRemoteCacheBuilders(remoteFactories);

            return globalCacheConfig;
        }

    }

    public static class ConfigTestBean {
        @CreateCache
        Cache defualtRemote;

        @CreateCache(cacheType = CacheType.LOCAL)
        Cache defaultLocal;

        @CreateCache(area = "A1")
        Cache a1Remote;

        @CreateCache(area = "A1", cacheType = CacheType.LOCAL)
        Cache a1Local;

        @CreateCache(expire = 1, serialPolicy = SerialPolicy.KRYO, keyConvertor = KeyConvertor.FASTJSON)
        Cache customRemote;

        @CreateCache(expire = 1, keyConvertor = KeyConvertor.FASTJSON, cacheType = CacheType.LOCAL, localLimit = 123)
        Cache customLocal;
    }
}
