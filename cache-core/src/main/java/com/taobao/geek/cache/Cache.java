/**
 * Created on  13-09-09 17:28
 */
package com.taobao.geek.cache;

/**
 * @author yeli.hl
 */
public interface Cache {

    public CacheResult get(byte[] key);

    public CacheResultCode put(byte[] key, Object value);
}
