package otherpackage;

import com.alicp.jetcache.Cached;

/**
 * Created by huangli on 16/3/2.
 */
public interface OtherService {
    @Cached
    int bar();
}
