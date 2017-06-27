package com.alicp.jetcache.anno.inittestbeans;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created on 2017/5/4.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
@Configuration
public class ConfigurationTest {

    @Bean
    public MethodCacheInitTestBean postConstructorTest1() {
        return new PostConstructorBean1();
    }

    @Bean
    public CreateCacheInitTestBean postConstructorTest2() {
        return new PostConstructorBean2();
    }

    @Bean
    public MethodCacheInitTestBean afterPropertiesSetTest1() {
        return new AfterPropertiesSetBean1();
    }

    @Bean
    public CreateCacheInitTestBean afterPropertiesSetTest2() {
        return new AfterPropertiesSetBean2();
    }
}
