package com.alicp.jetcache.anno.config;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.*;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import com.alicp.jetcache.anno.support.SpringConfigProvider;
import com.alicp.jetcache.embedded.EmbeddedCacheBuilder;
import com.alicp.jetcache.embedded.EmbeddedCacheConfig;
import com.alicp.jetcache.embedded.LinkedHashMapCacheBuilder;
import com.alicp.jetcache.external.ExternalCacheConfig;
import com.alicp.jetcache.support.*;
import com.alicp.jetcache.test.anno.MockRemoteCacheBuilder;
import com.alicp.jetcache.test.beans.MyFactoryBean;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/12/29.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ConfigTest.A.class)
public class ConfigTest implements ApplicationContextAware {

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
            Assert.assertNull(c.getKeyConvertor());
            Assert.assertSame(JavaValueEncoder.INSTANCE, c.getValueEncoder());
            Assert.assertSame(JavaValueDecoder.INSTANCE, c.getValueDecoder());
            Assert.assertFalse(c.isExpireAfterAccess());
            Assert.assertEquals(90, c.getDefaultExpireInMillis());

            c = (ExternalCacheConfig) bean.a1Remote.config();
            Assert.assertEquals(110, c.getDefaultExpireInMillis());

            c = (ExternalCacheConfig) bean.customRemote.config();
            Assert.assertEquals(1000, c.getDefaultExpireInMillis());
            Assert.assertSame(KryoValueEncoder.INSTANCE, c.getValueEncoder());
            Assert.assertSame(KryoValueDecoder.INSTANCE, c.getValueDecoder());
            Assert.assertSame(FastjsonKeyConvertor.INSTANCE, c.getKeyConvertor());
        }

        {
            EmbeddedCacheConfig c = (EmbeddedCacheConfig) bean.defaultLocal.config();
            Assert.assertNull(c.getKeyConvertor());
            Assert.assertEquals(20, c.getLimit());
            Assert.assertEquals(50, c.getDefaultExpireInMillis());
            Assert.assertFalse(c.isExpireAfterAccess());

            c = (EmbeddedCacheConfig) bean.a1Local.config();
            Assert.assertEquals(60, c.getDefaultExpireInMillis());

            c = (EmbeddedCacheConfig) bean.customLocal.config();
            Assert.assertEquals(1000, c.getDefaultExpireInMillis());
            Assert.assertEquals(123, c.getLimit());
            Assert.assertSame(FastjsonKeyConvertor.INSTANCE, c.getKeyConvertor());
        }
    }


    @Configuration
    @EnableMethodCache(basePackages = "com.alicp.jetcache.anno.config")
    @EnableCreateCacheAnnotation
    public static class A {
        @Bean
        public ConfigTestBean configTestBean() {
            return new ConfigTestBean();
        }

        @Bean
        public SpringConfigProvider springConfigProvider() {
            return new SpringConfigProvider();
        }

        @Bean(name = "factoryBeanTarget")
        public MyFactoryBean factoryBean() {
            return new MyFactoryBean();
        }

        @Bean
        public GlobalCacheConfig config(SpringConfigProvider configProvider) {
            Map localFactories = new HashMap();
            EmbeddedCacheBuilder localFactory = new LinkedHashMapCacheBuilder().createLinkedHashMapCacheBuilder()
                    .limit(20).keyConvertor(null).expireAfterWrite(50, TimeUnit.MILLISECONDS);
            EmbeddedCacheBuilder localFactory2 = new LinkedHashMapCacheBuilder().createLinkedHashMapCacheBuilder()
                    .limit(10).keyConvertor(FastjsonKeyConvertor.INSTANCE).expireAfterAccess(60, TimeUnit.MILLISECONDS);
            localFactories.put(CacheConsts.DEFAULT_AREA, localFactory);
            localFactories.put("A1", localFactory2);


            Map remoteFactories = new HashMap();

            MockRemoteCacheBuilder remoteBuilder = new MockRemoteCacheBuilder();
            remoteBuilder.setKeyConvertor(null);
            remoteBuilder.setValueEncoder(JavaValueEncoder.INSTANCE);
            remoteBuilder.setValueDecoder(JavaValueDecoder.INSTANCE);
            remoteBuilder.setExpireAfterAccess(false);
            remoteBuilder.setDefaultExpireInMillis(90);
            remoteFactories.put(CacheConsts.DEFAULT_AREA, remoteBuilder);

            remoteBuilder = new MockRemoteCacheBuilder();
            remoteBuilder.setKeyConvertor(FastjsonKeyConvertor.INSTANCE);
            remoteBuilder.setValueEncoder(KryoValueEncoder.INSTANCE);
            remoteBuilder.setValueDecoder(KryoValueDecoder.INSTANCE);
            remoteBuilder.setExpireAfterAccess(true);
            remoteBuilder.setDefaultExpireInMillis(110);
            remoteFactories.put("A1", remoteBuilder);

            GlobalCacheConfig globalCacheConfig = new GlobalCacheConfig();
            globalCacheConfig.setConfigProvider(configProvider);
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
