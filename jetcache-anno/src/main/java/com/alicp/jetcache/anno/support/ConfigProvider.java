package com.alicp.jetcache.anno.support;

import javax.annotation.Resource;
import java.util.function.Function;

/**
 * Created on 2016/11/29.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class ConfigProvider extends AbstractLifecycle {

    protected SimpleCacheManager cacheManager;
    protected EncoderParser encoderParser;
    protected KeyConvertorParser keyConvertorParser;
    protected CacheMonitorInstaller cacheMonitorInstaller;
    private CacheMonitorInstaller defaultCacheMonitorInstaller = new DefaultCacheMonitorInstaller();

    @Resource
    protected GlobalCacheConfig globalCacheConfig;

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
            ((DefaultCacheMonitorInstaller) cacheMonitorInstaller).setGlobalCacheConfig(globalCacheConfig);
            ((DefaultCacheMonitorInstaller) cacheMonitorInstaller).init();
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
}
