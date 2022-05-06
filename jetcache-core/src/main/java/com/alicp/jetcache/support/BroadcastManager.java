/**
 * Created on 2019/6/10.
 */
package com.alicp.jetcache.support;

import com.alicp.jetcache.CacheResult;

import java.util.function.Consumer;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public interface BroadcastManager {

    CacheResult publish(CacheMessage cacheMessage);

    void startSubscribe(Consumer<CacheMessage> consumer);

}
