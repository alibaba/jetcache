/**
 * Created on  13-09-19 21:53
 */
package com.alicp.jetcache.anno.config.beans;

import com.alicp.jetcache.anno.Cached;
import com.alicp.jetcache.anno.EnableCache;
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

    @Override
    @Cached
    public int countWithAnnoOnClass() {
        return count++;
    }

    @Override
    public int countWithAnnoOnInterface(){
        return count++;
    }

    @Override
    public int enableCacheWithAnnoOnInterface(TestBean bean){
        return bean.countWithDisabledCache();
    }

    @Override
    @EnableCache
    public int enableCacheWithAnnoOnClass(TestBean bean){
        return bean.countWithDisabledCache();
    }

    @Override
    @EnableCache
    public int enableCacheWithNoCacheCount(TestBean bean){
        return bean.noCacheCount();
    }

}
