package com.alicp.jetcache.anno.config.combined;

import com.alicp.jetcache.anno.Cached;
import org.springframework.stereotype.Component;

/**
 * Created on 2017/2/14.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
//@Component
public class ServiceImpl implements Service {

    private int count;

    @Override
    public int m1() {
        return count++;
    }

    @Override
    @Cached
    public int m2() {
        return count++;
    }
}
