package com.alicp.jetcache.autoconfigure;

import com.alicp.jetcache.anno.config.EnableMethodCache;
import com.alicp.jetcache.anno.config.SpringTest;
import com.alicp.jetcache.anno.config.beans.MyFactoryBean;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created on 2016/11/23.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
@SpringBootApplication(scanBasePackages = "com.alicp.jetcache.anno.config.beans")
@EnableMethodCache(basePackages = "com.alicp.jetcache.anno.config.beans")
public class StarterTest extends SpringTest {

    @Test
    public void tests() {
        context = SpringApplication.run(StarterTest.class);
        doTest();
    }

    @Configuration
    public static class A {
        @Bean("factoryBeanTarget")
        public MyFactoryBean factoryBean() {
            return new MyFactoryBean();
        }

//        @Bean
//        public Consumer<StatInfo> statCallback(){
//            return si -> {};
//        }
    }

}
