/**
 * Created on  13-09-19 21:53
 */
package com.alicp.jetcache.anno.spring.beans;

import com.alicp.jetcache.anno.Cached;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
@Component("service")
public class ServiceImpl implements Service {

    private int count;

    @Cached
    public int count() {
        return count++;
    }

    public int countWithAnnoOnInterface(){
        return count++;
    }

}
