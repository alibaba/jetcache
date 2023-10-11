/**
 * Created on 2018/8/11.
 */
package jetcache.samples.springboot;

import org.springframework.stereotype.Repository;

/**
 * @author huangli
 */
@Repository
public class UserServiceImpl implements UserService {

    @Override
    public User loadUser(long userId) {
        System.out.println("load user: " + userId);
        User user = new User();
        user.setUserId(userId);
        user.setUserName("user" + userId);
        return user;
    }
}
