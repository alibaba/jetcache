/**
 * Created on 2018/8/11.
 */
package jetcache.samples.springboot;

import com.alicp.jetcache.anno.config.EnableCreateCacheAnnotation;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
@SpringBootApplication
@EnableMethodCache(basePackages = "jetcache")
@EnableCreateCacheAnnotation
public class SpringBootApp {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(SpringBootApp.class);
        MyService myService = context.getBean(MyService.class);
        myService.createCacheDemo();
        myService.cachedDemo();
    }
}
