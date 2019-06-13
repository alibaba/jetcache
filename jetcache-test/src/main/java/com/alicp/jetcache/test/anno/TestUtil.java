/**
 * Created on  13-09-23 09:36
 */
package com.alicp.jetcache.test.anno;

import com.alicp.jetcache.anno.support.SpringConfigProvider;
import com.alicp.jetcache.external.MockRemoteCacheBuilder;
import com.alicp.jetcache.anno.CacheConsts;
import com.alicp.jetcache.anno.support.ConfigProvider;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import com.alicp.jetcache.embedded.EmbeddedCacheBuilder;
import com.alicp.jetcache.embedded.LinkedHashMapCacheBuilder;
import com.alicp.jetcache.support.FastjsonKeyConvertor;
import com.alicp.jetcache.support.KryoValueDecoder;
import com.alicp.jetcache.support.KryoValueEncoder;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class TestUtil {
    public static GlobalCacheConfig createGloableConfig() {
        Map localBuilders = new HashMap();
        EmbeddedCacheBuilder localBuilder = LinkedHashMapCacheBuilder.createLinkedHashMapCacheBuilder();
        localBuilder.setKeyConvertor(FastjsonKeyConvertor.INSTANCE);
        localBuilders.put(CacheConsts.DEFAULT_AREA, localBuilder);
        localBuilders.put("A1", localBuilder);

        Map remoteBuilders = new HashMap();

        MockRemoteCacheBuilder remoteBuilder = new MockRemoteCacheBuilder();
        remoteBuilder.setKeyConvertor(FastjsonKeyConvertor.INSTANCE);
        remoteBuilder.setValueEncoder(KryoValueEncoder.INSTANCE);
        remoteBuilder.setValueDecoder(KryoValueDecoder.INSTANCE);
        remoteBuilders.put(CacheConsts.DEFAULT_AREA, remoteBuilder);

        remoteBuilder = new MockRemoteCacheBuilder();
        remoteBuilder.setKeyConvertor(FastjsonKeyConvertor.INSTANCE);
        remoteBuilder.setValueEncoder(KryoValueEncoder.INSTANCE);
        remoteBuilder.setValueDecoder(KryoValueDecoder.INSTANCE);
        remoteBuilders.put("A1", remoteBuilder);

        GlobalCacheConfig globalCacheConfig = new GlobalCacheConfig();
        globalCacheConfig.setLocalCacheBuilders(localBuilders);
        globalCacheConfig.setRemoteCacheBuilders(remoteBuilders);

        return globalCacheConfig;
    }

    public static ConfigProvider createConfigProvider() {
        ConfigProvider configProvider = new ConfigProvider();
        configProvider.setGlobalCacheConfig(createGloableConfig());
        return configProvider;
    }

    public static SpringConfigProvider createSpringConfigProvider() {
        SpringConfigProvider configProvider = new SpringConfigProvider();
        configProvider.setGlobalCacheConfig(createGloableConfig());
        return configProvider;
    }

}
