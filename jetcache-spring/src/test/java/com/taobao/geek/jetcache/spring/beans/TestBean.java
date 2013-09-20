/**
 * Created on  13-09-17 11:26
 */
package com.taobao.geek.jetcache.spring.beans;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * @author yeli.hl
 */
@Component
public class TestBean {

    static int count = 0;


    public TestBean(){
    }

    @Cacheable("books")
    public int foo() {
        return count++;
    }

}
