/**
 * Created on 2018/8/11.
 */
package jetcache.samples.springboot;

import com.alicp.jetcache.anno.Cached;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public interface UserService {
    @Cached(name = "loadUser", expire = 10)
    User loadUser(long userId);
}
