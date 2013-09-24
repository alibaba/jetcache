/**
 * Created on  13-09-17 11:26
 */
package com.taobao.geek.jetcache.spring.beans;

import com.taobao.geek.jetcache.Cached;
import org.springframework.stereotype.Component;

/**
 * @author yeli.hl
 */
@Component
public class TestBean {

    private int count = 0;


    public TestBean() {
    }

    @Cached
    public int count() {
        return count++;
    }

    @Cached(enabled = false)
    public int countWithDisabledCache(){
        return count++;
    }

    @Cached(area = "A1")
    public int count(DynamicQuery q) {
        return count++;
    }

}
