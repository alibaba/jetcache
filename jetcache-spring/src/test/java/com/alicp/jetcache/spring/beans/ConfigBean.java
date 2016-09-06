/**
 * Created on  13-10-07 23:30
 */
package com.alicp.jetcache.spring.beans;

import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
@Component
public class ConfigBean {
    public boolean isTrueProp() {
         return true;
    }

    public boolean isFalseProp() {
        return false;
    }
}
