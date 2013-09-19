/**
 * Created on  13-09-19 21:53
 */
package com.taobao.geek.jetcache.spring.beans;

import org.springframework.stereotype.Component;

/**
 * @author yeli.hl
 */
@Component
public class ServiceImpl implements Service {
    @Override
    public int foo(){
        return 0;
    }

}
