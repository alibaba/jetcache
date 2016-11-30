package com.alicp.jetcache.autoconfigure;

import com.alicp.jetcache.anno.TestUtil;
import com.alicp.jetcache.anno.config.EnableJetCache;
import com.alicp.jetcache.anno.config.SpringTest;
import com.alicp.jetcache.anno.config.beans.MyFactoryBean;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import com.alicp.jetcache.anno.support.SpringGlobalCacheConfig;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
//@EnableJetCache(basePackages = "com.alicp.jetcache.anno.config.beans")
public class StarterTest extends SpringTest {

//    @Test
//    public void test() {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication();
//        context = app.run(StarterTest.class);
//        JetCacheProperties bean = context.getBean(JetCacheProperties.class);
//        doTest();
    }

//    @Bean
//    public SpringGlobalCacheConfig config(){
//        return (SpringGlobalCacheConfig) TestUtil.createGloableConfig(SpringGlobalCacheConfig::new);
//    }

    @Bean("factoryBeanTarget")
    public MyFactoryBean factoryBean(){
        return new MyFactoryBean();
    }

}
