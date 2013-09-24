/**
 * Created on  13-09-19 21:49
 */
package com.taobao.geek.jetcache.spring.beans;

import com.taobao.geek.jetcache.Cached;

/**
 * @author yeli.hl
 */
public interface Service {
    int count();

    @Cached
    int countWithAnnoOnInterface();
}
