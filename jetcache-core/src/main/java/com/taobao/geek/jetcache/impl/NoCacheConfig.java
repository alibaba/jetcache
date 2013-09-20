/**
 * Created on  13-09-19 23:46
 */
package com.taobao.geek.jetcache.impl;

import com.taobao.geek.jetcache.CacheConfig;

/**
 * @author yeli.hl
 */
public class NoCacheConfig extends CacheConfig {
    private static NoCacheConfig instance = new NoCacheConfig();
    static{
        instance.setArea("__NULL__");
        instance.setEnabled(false);
    }

    public static NoCacheConfig instance() {
        return instance;
    }
}
