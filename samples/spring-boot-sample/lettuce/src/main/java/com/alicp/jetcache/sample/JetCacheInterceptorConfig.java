package com.alicp.jetcache.sample;

import com.alicp.jetcache.autoconfigure.AutoConfigureBeans;
import com.alicp.jetcache.external.ExternalCacheWriteInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * JetCache Interceptor 配置示例
 * 展示如何注册自定义的 WriteInterceptor 到 JetCache 配置中
 *
 * @author jetcache
 */
@Configuration
public class JetCacheInterceptorConfig {

    private static final Logger logger = LoggerFactory.getLogger(JetCacheInterceptorConfig.class);

    @Autowired
    private AutoConfigureBeans autoConfigureBeans;

    /**
     * 方式一: 直接注册为 Bean，用于 @Cached 注解配置
     * Bean 名称需要在 @Cached.writeInterceptors 中引用
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
     * 方式二: 注册到 customContainer，用于 application.yml 配置
     * 使用 "interceptor." 前缀
     */
    @PostConstruct
    public void registerInterceptors() {
        autoConfigureBeans.getCustomContainer().put("interceptor.logging", loggingInterceptor());
        autoConfigureBeans.getCustomContainer().put("interceptor.audit", auditInterceptor());
    }
}
