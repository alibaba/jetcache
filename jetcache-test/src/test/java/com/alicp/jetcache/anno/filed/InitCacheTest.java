package com.alicp.jetcache.anno.filed;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CreateCache;
import com.alicp.jetcache.anno.field.InitCacheAnnotationBeanPostProcessor;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.PostConstruct;

/**
 * Created on 2016/12/9.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(classes = InitCacheTest.A.class)
public class InitCacheTest {


//    @Test
//    public void test(){
//    }

    public static void main(String args[]) {
        AnnotationConfigApplicationContext c = new AnnotationConfigApplicationContext();
        c.register(A.class);
        c.refresh();
    }

    @Configuration
    public static class A {

        @Bean
        public Foo foo(){
            return new Foo();
        }


//        @Bean
//        public AutowiredAnnotationBeanPostProcessor getAutowiredAnnotationBeanPostProcessor() {
//            AutowiredAnnotationBeanPostProcessor autowiredAnnotationBeanPostProcessor = new AutowiredAnnotationBeanPostProcessor();
//            autowiredAnnotationBeanPostProcessor.setAutowiredAnnotationType(CreateCache.class);
//            return autowiredAnnotationBeanPostProcessor;
//        }

        @Bean
        public InitCacheAnnotationBeanPostProcessor initCacheAnnotationBeanPostProcessor(){
            return new InitCacheAnnotationBeanPostProcessor();
        }

        public static class Foo {
            @CreateCache
            private Cache cache;

        }

        @Bean
        public GlobalCacheConfig globalCacheConfig(){
            return new GlobalCacheConfig();
        }
    }


}
