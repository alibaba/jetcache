package com.alicp.jetcache.anno;

import com.alicp.jetcache.anno.config.EnableJetCache;
import com.alicp.jetcache.anno.springtest.TestUtil;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created on 2016/11/16.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class Test {
    public static void main(String[] args) {
        ApplicationContext ctx =
                new AnnotationConfigApplicationContext(A.class);
        A a = ctx.getBean(A.class);
        System.out.println(a.foo());
        System.out.println(a.foo());
    }

    @EnableJetCache(basePackages = "com.alicp.jetcache.anno.Test")
    public static class A {

        int count;

        @Cached
        public int foo(){
            return count++;
        }

        @Configuration
        public static class B{
            @Bean
            public GlobalCacheConfig globalCacheConfig(){
                return TestUtil.createGloableConfig();
            }

        }
    }



}
