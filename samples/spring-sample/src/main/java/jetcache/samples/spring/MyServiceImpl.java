/**
 * Created on 2018/8/11.
 */
package jetcache.samples.spring;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.template.QuickConfig;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * @author huangli
 */
@Component
public class MyServiceImpl implements MyService, InitializingBean {
    @Autowired
    private UserService userService;

    @Autowired
    private CacheManager cacheManager;

    private Cache<String, String> orderCache;

    @Override
    public void afterPropertiesSet() {
        QuickConfig quickConfig = QuickConfig.newBuilder("orderCache").expire(Duration.ofSeconds(100)).build();
        orderCache = cacheManager.getOrCreateCache(quickConfig);
    }

    @Override
    public void createCacheDemo() {
        orderCache.put("K1","V1");
        System.out.println("get from orderCache:" + orderCache.get("K1"));
    }

    @Override
    public void cachedDemo() {
        userService.loadUser(1);
        userService.loadUser(1);
    }
}
