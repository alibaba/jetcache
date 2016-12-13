/**
 * Created on  13-09-23 09:36
 */
package com.alicp.jetcache.anno;

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
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class TestUtil {
    public static GlobalCacheConfig createGloableConfig(ConfigProvider configProvider) {
        Map localFactories = new HashMap();
        EmbeddedCacheBuilder localFactory = new LinkedHashMapCacheBuilder();
        localFactory.setKeyConvertor(FastjsonKeyConvertor.INSTANCE);
        localFactories.put(CacheConsts.DEFAULT_AREA, localFactory);
        localFactories.put("A1", localFactory);

        Map remoteFactories = new HashMap();

        MockRemoteCacheBuilder remoteBuilder = new MockRemoteCacheBuilder();
        remoteBuilder.setKeyConvertor(FastjsonKeyConvertor.INSTANCE);
        remoteBuilder.setValueEncoder(KryoValueEncoder.INSTANCE);
        remoteBuilder.setValueDecoder(KryoValueDecoder.INSTANCE);
        remoteFactories.put(CacheConsts.DEFAULT_AREA, remoteBuilder);

        remoteBuilder = new MockRemoteCacheBuilder();
        remoteBuilder.setKeyConvertor(FastjsonKeyConvertor.INSTANCE);
        remoteBuilder.setValueEncoder(KryoValueEncoder.INSTANCE);
        remoteBuilder.setValueDecoder(KryoValueDecoder.INSTANCE);
        remoteFactories.put("A1", remoteBuilder);

        GlobalCacheConfig globalCacheConfig = new GlobalCacheConfig();
        globalCacheConfig.setConfigProvider(configProvider);
        globalCacheConfig.setLocalCacheBuilders(localFactories);
        globalCacheConfig.setRemoteCacheBuilders(remoteFactories);

//        globalCacheConfig.init();
        return globalCacheConfig;
    }

}
