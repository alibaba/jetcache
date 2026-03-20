package com.alicp.jetcache.sample;

import com.alicp.jetcache.anno.Cached;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 使用 @Cached 配置外部缓存写入前回调的示例。
 *
 * @author jetcache
 */
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Cached(
        name = "user",
        expire = 3600,
        timeUnit = TimeUnit.SECONDS,
        externalWriteInterceptors = "bean:loggingInterceptor,bean:auditInterceptor"
    )
    public User getUserById(Long id) {
        logger.info("Fetching user from database: {}", id);
        return new User(id, "User" + id, "user" + id + "@example.com");
    }

    @Cached(
        name = "userInfo",
        expire = 1800,
        timeUnit = TimeUnit.SECONDS,
        externalWriteInterceptors = "bean:loggingInterceptor"
    )
    public String getUserInfo(Long id) {
        logger.info("Fetching user info from database: {}", id);
        return "User Info for " + id;
    }

    /**
     * 用户实体类
     */
    public static class User {
        private Long id;
        private String name;
        private String email;

        public User(Long id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }
    }
}
