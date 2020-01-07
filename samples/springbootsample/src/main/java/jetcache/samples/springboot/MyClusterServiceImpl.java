package jetcache.samples.springboot;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CreateCache;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author eason.feng at 2019/12/18/0018 15:39
 **/
public class MyClusterServiceImpl implements MyService {

    @CreateCache(area = "mycluster")
    private Cache<String, String> clusterCache;

    @Autowired
    private UserService userService;

    @Override
    public void createCacheDemo() {
        clusterCache.put("clusterMyKey", "clusterMyValue");
        String clusterMyValue = clusterCache.get("clusterMyKey");
        System.out.println("get 'clusterMyKey' from clusterCache:" + clusterMyValue);
    }

    @Override
    public void cachedDemo() {
        userService.loadUserWithJedisCluster(1);
        userService.loadUserWithJedisCluster(1);
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
