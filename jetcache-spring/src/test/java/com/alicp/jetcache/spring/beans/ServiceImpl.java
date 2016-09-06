/**
 * Created on  13-09-19 21:53
 */
package com.alicp.jetcache.spring.beans;

import com.alicp.jetcache.Cached;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
@Component("service")
public class ServiceImpl implements Service {

    private int count;

    @Override
    @Cached
    public int count() {
        return count++;
    }

    @Override
    public int countWithAnnoOnInterface(){
        return count++;
    }

}
