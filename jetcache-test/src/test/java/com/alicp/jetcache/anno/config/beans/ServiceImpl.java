/**
 * Created on  13-09-19 21:53
 */
package com.alicp.jetcache.anno.config.beans;

import com.alicp.jetcache.anno.Cached;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
@Component("service")
public class ServiceImpl implements Service {

    private int count;

    @Override
    public int notCachedCount() {
        return count++;
    }

    @Cached
    public int countWithAnnoOnClass() {
        return count++;
    }

    public int countWithAnnoOnInterface(){
        return count++;
    }

}
