/**
 * Created on  13-10-07 23:30
 */
package com.alicp.jetcache.test.beans;

import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
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
