package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.support.CacheUpdatePublisher;
import com.alicp.jetcache.support.CacheUpdateReceiver;
import com.alicp.jetcache.support.StatInfo;
import com.alicp.jetcache.support.StatInfoLogger;

import javax.annotation.Resource;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created on 2016/11/29.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class ConfigProvider extends AbstractLifecycle {

    @Resource
    protected GlobalCacheConfig globalCacheConfig;

    protected SimpleCacheManager cacheManager;
    protected EncoderParser encoderParser;
    protected KeyConvertorParser keyConvertorParser;
    protected CacheMonitorInstaller cacheMonitorInstaller;
    private Consumer<StatInfo> statCallback = new StatInfoLogger(false);
    private CacheUpdatePublisher cacheUpdatePublisher;

    private CacheMonitorInstaller defaultCacheMonitorInstaller = new DefaultCacheMonitorInstaller();

    private CacheContext cacheContext;

    public ConfigProvider() {
        cacheManager = SimpleCacheManager.defaultManager;
        encoderParser = new DefaultEncoderParser();
        keyConvertorParser = new DefaultKeyConvertorParser();
        cacheMonitorInstaller = defaultCacheMonitorInstaller;
    }

    @Override
    public void doInit() {
        initDefaultCacheMonitorInstaller();
        cacheContext = newContext();
    }

    protected void initDefaultCacheMonitorInstaller() {
        if (cacheMonitorInstaller == defaultCacheMonitorInstaller) {
            DefaultCacheMonitorInstaller installer = (DefaultCacheMonitorInstaller) cacheMonitorInstaller;
            installer.setGlobalCacheConfig(globalCacheConfig);
            installer.setStatCallback(statCallback);
            if (cacheUpdatePublisher != null) {
                installer.setCacheUpdatePublisher(cacheUpdatePublisher);
            }
            installer.init();
        }
        cacheContext = null;
    }

    @Override
    public void doShutdown() {
        shutdownDefaultCacheMonitorInstaller();
        cacheManager.rebuild();
    }

    protected void shutdownDefaultCacheMonitorInstaller() {
        if (cacheMonitorInstaller == defaultCacheMonitorInstaller) {
            ((DefaultCacheMonitorInstaller) cacheMonitorInstaller).shutdown();
        }
    }

    /**
     * Keep this method for backward compatibility.
     * NOTICE: there is no getter for encoderParser.
     */
    public Function<Object, byte[]> parseValueEncoder(String valueEncoder) {
        return encoderParser.parseEncoder(valueEncoder);
    }

    /**
     * Keep this method for backward compatibility.
     * NOTICE: there is no getter for encoderParser.
     */
    public Function<byte[], Object> parseValueDecoder(String valueDecoder) {
        return encoderParser.parseDecoder(valueDecoder);
    }

    /**
     * Keep this method for backward compatibility.
     * NOTICE: there is no getter for keyConvertorParser.
     */
    public Function<Object, Object> parseKeyConvertor(String convertor) {
        return keyConvertorParser.parseKeyConvertor(convertor);
    }

    public CacheNameGenerator createCacheNameGenerator(String[] hiddenPackages) {
        return new DefaultCacheNameGenerator(hiddenPackages);
    }

    protected CacheContext newContext() {
        return new CacheContext(this, globalCacheConfig);
    }

    public void setCacheManager(SimpleCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public SimpleCacheManager getCacheManager() {
        return cacheManager;
    }

    public void setEncoderParser(EncoderParser encoderParser) {
        this.encoderParser = encoderParser;
    }

    public void setKeyConvertorParser(KeyConvertorParser keyConvertorParser) {
        this.keyConvertorParser = keyConvertorParser;
    }

    public CacheMonitorInstaller getCacheMonitorInstaller() {
        return cacheMonitorInstaller;
    }

    public void setCacheMonitorInstaller(CacheMonitorInstaller cacheMonitorInstaller) {
        this.cacheMonitorInstaller = cacheMonitorInstaller;
    }

    public GlobalCacheConfig getGlobalCacheConfig() {
        return globalCacheConfig;
    }

    public void setGlobalCacheConfig(GlobalCacheConfig globalCacheConfig) {
        this.globalCacheConfig = globalCacheConfig;
    }

    public CacheContext getCacheContext() {
        return cacheContext;
    }

    public void setStatCallback(Consumer<StatInfo> statCallback) {
        this.statCallback = statCallback;
    }

    public void setCacheUpdatePublisher(CacheUpdatePublisher cacheUpdatePublisher) {
        this.cacheUpdatePublisher = cacheUpdatePublisher;
    }
}
