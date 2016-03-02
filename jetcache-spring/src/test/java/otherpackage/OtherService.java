package otherpackage;

import com.taobao.geek.jetcache.Cached;

/**
 * Created by huangli on 16/3/2.
 */
public interface OtherService {
    @Cached
    int bar();
}
