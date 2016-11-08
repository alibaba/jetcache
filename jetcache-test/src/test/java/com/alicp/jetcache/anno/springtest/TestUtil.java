/**
 * Created on  13-09-23 09:36
 */
package com.alicp.jetcache.anno.springtest;

import com.alicp.jetcache.anno.CacheConsts;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import com.alicp.jetcache.factory.EmbeddedCacheFactory;
import com.alicp.jetcache.factory.LinkedHashMapCacheFactory;
import com.alicp.jetcache.support.FastjsonKeyConvertor;
import com.alicp.jetcache.support.KryoValueDecoder;
import com.alicp.jetcache.support.KryoValueEncoder;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class TestUtil {
    public static GlobalCacheConfig createGloableConfig() {
        EmbeddedCacheFactory localFactory = new LinkedHashMapCacheFactory();
        localFactory.setKeyConvertor(FastjsonKeyConvertor.INSTANCE);
        Map localFactories = new HashMap();
        localFactories.put(CacheConsts.DEFAULT_AREA, localFactory);

        MockRemoteCacheFactory remoteFactory = new MockRemoteCacheFactory();
        remoteFactory.setKeyConvertor(FastjsonKeyConvertor.INSTANCE);
        remoteFactory.setValueEncoder(KryoValueEncoder.INSTANCE);
        remoteFactory.setValueDecoder(KryoValueDecoder.INSTANCE);
        Map remoteFactories = new HashMap();
        remoteFactories.put(CacheConsts.DEFAULT_AREA, remoteFactory);

        GlobalCacheConfig globalCacheConfig = new GlobalCacheConfig();
        globalCacheConfig.setLocalCacheFacotories(localFactories);
        globalCacheConfig.setRemoteCacheFacotories(remoteFactories);
        return globalCacheConfig;
    }

}
