/**
 * Created on  13-09-19 21:53
 */
package com.alicp.jetcache.test.beans;

import com.alicp.jetcache.anno.Cached;
import com.alicp.jetcache.anno.EnableCache;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
@Component("service")
public class ServiceImpl implements Service {

    private int count;
    Map<String, Integer> m = new HashMap<>();

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

    @Override
    public int count(String id) {
        Integer v = m.get(id);
        if (v == null) {
            v = count++;
        }
        v++;
        m.put(id, v);
        return v;
    }

    @Override
    public void update(String id, int value) {
        m.put(id, value);
    }

    @Override
    public void delete(String id) {
        m.remove(id);
    }

    @Override
    public void update2(String id, int value) {
        m.put(id, value);
    }

    @Override
    public void delete2(String id) {
        m.remove(id);
    }

}
