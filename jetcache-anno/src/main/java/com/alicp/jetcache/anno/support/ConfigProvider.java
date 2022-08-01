package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.CacheBuilder;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.embedded.EmbeddedCacheBuilder;
import com.alicp.jetcache.external.ExternalCacheBuilder;
import com.alicp.jetcache.support.StatInfo;
import com.alicp.jetcache.support.StatInfoLogger;
import com.alicp.jetcache.template.CacheBuilderTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created on 2016/11/29.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class ConfigProvider extends AbstractLifecycle {

    private static final Logger logger = LoggerFactory.getLogger(ConfigProvider.class);

    @Resource
    protected GlobalCacheConfig globalCacheConfig;

    protected CacheManager cacheManager;
    protected EncoderParser encoderParser;
    protected KeyConvertorParser keyConvertorParser;
    protected CacheMonitorManager cacheMonitorManager;
    private Consumer<StatInfo> metricsCallback = new StatInfoLogger(false);

    private final CacheMonitorManager defaultCacheMonitorManager = new DefaultCacheMonitorManager();

    private CacheContext cacheContext;

    public ConfigProvider() {
        cacheManager = CacheManager.defaultManager();
        encoderParser = new DefaultEncoderParser();
        keyConvertorParser = new DefaultKeyConvertorParser();
        cacheMonitorManager = defaultCacheMonitorManager;
    }

    @Override
    protected void doInit() {
        initDefaultCacheMonitorInstaller();
        cacheContext = newContext();
        CacheBuilderTemplate t = new CacheBuilderTemplate(globalCacheConfig.isPenetrationProtect(),
                globalCacheConfig.getLocalCacheBuilders(), globalCacheConfig.getRemoteCacheBuilders());
        for (CacheBuilder builder : globalCacheConfig.getLocalCacheBuilders().values()) {
            EmbeddedCacheBuilder eb = (EmbeddedCacheBuilder) builder;
            if (eb.getConfig().getKeyConvertor() instanceof ParserFunction) {
                ParserFunction f = (ParserFunction) eb.getConfig().getKeyConvertor();
                eb.setKeyConvertor(parseKeyConvertor(f.getValue()));
            }
        }
        for (CacheBuilder builder : globalCacheConfig.getRemoteCacheBuilders().values()) {
            ExternalCacheBuilder eb = (ExternalCacheBuilder) builder;
            if (eb.getConfig().getKeyConvertor() instanceof ParserFunction) {
                ParserFunction f = (ParserFunction) eb.getConfig().getKeyConvertor();
                eb.setKeyConvertor(parseKeyConvertor(f.getValue()));
            }
            if (eb.getConfig().getValueEncoder() instanceof ParserFunction) {
                ParserFunction f = (ParserFunction) eb.getConfig().getValueEncoder();
                eb.setValueEncoder(parseValueEncoder(f.getValue()));
            }
            if (eb.getConfig().getValueDecoder() instanceof ParserFunction) {
                ParserFunction f = (ParserFunction) eb.getConfig().getValueDecoder();
                eb.setValueDecoder(parseValueDecoder(f.getValue()));
            }
        }
        cacheManager.setCacheBuilderTemplate(t);
    }

    protected void initDefaultCacheMonitorInstaller() {
        if (cacheMonitorManager == defaultCacheMonitorManager) {
            DefaultCacheMonitorManager cacheMonitorManager = (DefaultCacheMonitorManager) this.cacheMonitorManager;
            cacheMonitorManager.setGlobalCacheConfig(globalCacheConfig);
            cacheMonitorManager.setMetricsCallback(metricsCallback);
            cacheMonitorManager.setConfigProvider(this);
            cacheMonitorManager.init();
        }
    }

    @Override
    public void doShutdown() {
        try {
            shutdownDefaultCacheMonitorInstaller();
            if (cacheManager instanceof AutoCloseable) {
                ((AutoCloseable) cacheManager).close();
            }
        } catch (Exception e) {
            logger.error("close fail", e);
        }
    }

    protected void shutdownDefaultCacheMonitorInstaller() {
        if (cacheMonitorManager == defaultCacheMonitorManager) {
            ((AbstractLifecycle) cacheMonitorManager).shutdown();
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

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public void setEncoderParser(EncoderParser encoderParser) {
        this.encoderParser = encoderParser;
    }

    public void setKeyConvertorParser(KeyConvertorParser keyConvertorParser) {
        this.keyConvertorParser = keyConvertorParser;
    }

    public CacheMonitorManager getCacheMonitorManager() {
        return cacheMonitorManager;
    }

    public void setCacheMonitorManager(CacheMonitorManager cacheMonitorManager) {
        this.cacheMonitorManager = cacheMonitorManager;
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

    public void setMetricsCallback(Consumer<StatInfo> metricsCallback) {
        this.metricsCallback = metricsCallback;
    }

}
