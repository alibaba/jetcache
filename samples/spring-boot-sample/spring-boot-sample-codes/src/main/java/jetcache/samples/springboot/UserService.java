/**
 * Created on 2018/8/11.
 */
package jetcache.samples.springboot;

import com.alicp.jetcache.anno.Cached;

/**
 * @author huangli
 */
public interface UserService {
    @Cached(name = "loadUser", expire = 10,externalWriteInterceptors = "bean:loggingExternalCacheWriteInterceptor")
    User loadUser(long userId);
}
