import com.alicp.jetcache.Cache;
import com.alicp.jetcache.embedded.CaffeineCacheBuilder;
import com.alicp.jetcache.support.FastjsonKeyConvertor;

import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/11/2.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class ComplexKeyExample {
    public static void main(String[] args) {
        Cache<Object, Object> cache = CaffeineCacheBuilder.createCaffeineCacheBuilder()
                .limit(100)
                .expireAfterWrite(200, TimeUnit.SECONDS)
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .buildCache();

        DynamicQuery key = new DynamicQuery();
        key.setName("AAA");
        key.setEmail("BBB");
        cache.get(key);
    }


    // no "equals" method
    static class DynamicQuery {
        private long id;
        private String name;
        private String email;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}
