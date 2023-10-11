/**
 * Created on 2022/08/06.
 */
package jetcache.samples.sync;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.template.QuickConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.time.Duration;

/**
 * @author huangli
 */
@SpringBootApplication
public class CacheUpdater {
    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext context = SpringApplication.run(CacheUpdater.class);
        CacheManager cm = context.getBean(CacheManager.class);
        QuickConfig qc = QuickConfig.newBuilder("spring-boot-sync-test")
                .cacheType(CacheType.BOTH)
                .syncLocal(true)
                .expire(Duration.ofSeconds(200))
                .build();
        Cache<String, Integer> c = cm.getOrCreateCache(qc);
        for (int i = 0; i < 100; i++) {
            c.put("MyKey", i);
            System.out.println("put " + i);
            Thread.sleep(1000);
        }
    }
}
