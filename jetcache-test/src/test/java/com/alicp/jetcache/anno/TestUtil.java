/**
 * Created on  13-09-23 09:36
 */
package com.alicp.jetcache.anno;

import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import com.alicp.jetcache.embedded.EmbeddedCacheBuilder;
import com.alicp.jetcache.embedded.LinkedHashMapCacheBuilder;
import com.alicp.jetcache.support.FastjsonKeyConvertor;
import com.alicp.jetcache.support.KryoValueDecoder;
import com.alicp.jetcache.support.KryoValueEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class TestUtil {
    public static GlobalCacheConfig createGloableConfig(Supplier<GlobalCacheConfig> creator) {
        Map localFactories = new HashMap();
        EmbeddedCacheBuilder localFactory = new LinkedHashMapCacheBuilder();
        localFactory.setKeyConvertor(FastjsonKeyConvertor.INSTANCE);
        localFactories.put(CacheConsts.DEFAULT_AREA, localFactory);
        localFactories.put("A1", localFactory);

        Map remoteFactories = new HashMap();

        MockRemoteCacheBuilder remoteFactory = new MockRemoteCacheBuilder();
        remoteFactory.setKeyConvertor(FastjsonKeyConvertor.INSTANCE);
        remoteFactory.setValueEncoder(KryoValueEncoder.INSTANCE);
        remoteFactory.setValueDecoder(KryoValueDecoder.INSTANCE);
        remoteFactories.put(CacheConsts.DEFAULT_AREA, remoteFactory);

        remoteFactory = new MockRemoteCacheBuilder();
        remoteFactory.setKeyConvertor(FastjsonKeyConvertor.INSTANCE);
        remoteFactory.setValueEncoder(KryoValueEncoder.INSTANCE);
        remoteFactory.setValueDecoder(KryoValueDecoder.INSTANCE);
        remoteFactories.put("A1", remoteFactory);

        GlobalCacheConfig globalCacheConfig = creator.get();
        globalCacheConfig.setLocalCacheBuilders(localFactories);
        globalCacheConfig.setRemoteCacheBuilders(remoteFactories);

//        globalCacheConfig.init();
        return globalCacheConfig;
    }

}
