/**
 * Created on  13-09-09 15:47
 */
package com.taobao.geek.cache;

/**
 * @author yeli.hl
 */
public interface TestIntf {
    @Cached
    String getValue(long id);
}
