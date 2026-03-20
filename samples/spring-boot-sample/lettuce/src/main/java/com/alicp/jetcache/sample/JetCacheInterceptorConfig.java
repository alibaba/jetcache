package com.alicp.jetcache.sample;

import com.alicp.jetcache.autoconfigure.AutoConfigureBeans;
import com.alicp.jetcache.external.ExternalCacheWriteInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * JetCache 写入前回调配置示例。
 * 注解方式使用 Spring Bean 名称加 {@code bean:} 前缀，
 * starter yml 方式既可以使用 {@code bean:xxx}，也可以使用 customContainer 中的普通名称。
 *
 * @author jetcache
 */
@Configuration
public class JetCacheInterceptorConfig {

    @Autowired
    private AutoConfigureBeans autoConfigureBeans;

    /**
     * 方式一: 直接注册为 Bean，用于@Cached/@CreateCache 注解配置
     * Bean 名称需要在 @Cached.externalWriteInterceptors/@CreateCache.externalWriteInterceptors 中以 bean: 前缀引用
     */
    @Bean("loggingInterceptor")
    public ExternalCacheWriteInterceptor loggingInterceptor() {
        return new MyWriteInterceptor("logging");
    }

    @Bean("auditInterceptor")
    public ExternalCacheWriteInterceptor auditInterceptor() {
        return new MyWriteInterceptor("audit");
    }

    /**
     * 方式二: 注册到 customContainer，用于 starter application.yml 配置。
     * 使用普通名称（不加 bean:）时会从 customContainer 取值。
     */
    @PostConstruct
    public void registerInterceptors() {
        autoConfigureBeans.getCustomContainer().put("logging", loggingInterceptor());
        autoConfigureBeans.getCustomContainer().put("audit", auditInterceptor());
    }
}
