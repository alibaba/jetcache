import com.alicp.jetcache.Cache;
import com.alicp.jetcache.embedded.CaffeineCacheBuilder;

import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/11/2.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class SimpleCacheExample {
    public static void main(String[] args) {
        Cache<String, Integer> cache = CaffeineCacheBuilder.createCaffeineCacheBuilder()
                .limit(100)
                .expireAfterWrite(200, TimeUnit.SECONDS)
                .buildCache();
        cache.put("20161111", 1000000, 1 ,TimeUnit.HOURS);
        Integer orderCount1 = cache.get("20161111");
        Integer orderCount2 = cache.computeIfAbsent("20161212", SimpleCacheExample::loadFromDatabase);
        System.out.println(orderCount1);
        System.out.println(orderCount2);
        cache.remove("20161212");
    }

    private static Integer loadFromDatabase(String key) {
        //...
        return 1000;
    }
}
