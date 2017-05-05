package com.alicp.jetcache.anno.inittestbeans;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created on 2017/5/4.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
@Configuration
public class ConfigurationTest {

    @Bean
    public InitTestBean postConstructorTest() {
        return new PostConstructorTestBean();
    }

    @Bean
    public InitTestBean afterPropertiesSetTest() {
        return new AfterPropertiesSetTestBean();
    }


}
