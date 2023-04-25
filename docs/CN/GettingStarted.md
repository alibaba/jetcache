
# 创建缓存实例
通过@CreateCache注解创建一个缓存实例，默认超时时间是100秒
```java
@Autowired
private CacheManager cacheManager;

private Cache<Long, UserDO> userCache;

@PostConstruct
public void init() {
    QuickConfig qc = QuickConfig.newBuilder("userCache") // name用于统计信息展示名字
        .expire(Duration.ofSeconds(100))
        //.cacheType(CacheType.BOTH) // 创建一个两级缓存
        //.localLimit(100) // 本地缓存元素个数限制，只对CacheType.LOCAL和CacheType.BOTH有效
        //.syncLocal(true) // 两级缓存的情况下，缓存更新时发消息让其它JVM实例中的缓存失效，需要配置broadcastChannel才生效。
        .build();
    userCache = cacheManager.getOrCreateCache(qc);
}
```

用起来就像map一样：

```java
UserDO user = userCache.get(123L);
userCache.put(123L, user);
userCache.remove(123L);
```

# 创建方法缓存
使用@Cached方法可以为一个方法添加上缓存。JetCache通过Spring AOP生成代理，来支持缓存功能。注解可以加在接口方法上也可以加在类方法上，但需要保证是个Spring bean。
```java
public interface UserService {
    @Cached(name="UserService.getUserById", expire = 3600)
    User getUserById(long userId);
}
```

# 基本配置（使用Spring Boot）

如果使用Spring Boot，可以按如下的方式配置（这里使用了jedis客户端连接redis，也可以使用[lettuce](RedisWithLettuce.md)客户端）。
# POM
```xml
<dependency>
    <groupId>com.alicp.jetcache</groupId>
    <artifactId>jetcache-starter-redis</artifactId>
    <version>${jetcache.latest.version}</version>
</dependency>
```
配置一个spring boot风格的application.yml文件，把他放到资源目录中
```
jetcache:
  statIntervalMinutes: 15
  areaInCacheName: false
  local:
    default:
      type: linkedhashmap
      keyConvertor: fastjson
  remote:
    default:
      type: redis
      keyConvertor: fastjson2
      broadcastChannel: projectA
      valueEncoder: java
      valueDecoder: java
      poolConfig:
        minIdle: 5
        maxIdle: 20
        maxTotal: 50
      host: 127.0.0.1
      port: 6379
```
然后创建一个App类放在业务包的根下，EnableMethodCache，EnableCreateCacheAnnotation这两个注解分别激活Cached和CreateCache注解，其他和标准的Spring Boot程序是一样的。这个类可以直接main方法运行。
```java
package com.company.mypackage;

import com.alicp.jetcache.anno.config.EnableCreateCacheAnnotation;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableMethodCache(basePackages = "com.company.mypackage")
@EnableCreateCacheAnnotation
public class MySpringBootApp {
    public static void main(String[] args) {
        SpringApplication.run(MySpringBootApp.class);
    }
}

```

# 未使用SpringBoot的配置方式
如果没有使用spring boot，可以按下面的方式配置（这里使用jedis客户端连接redis为例）。
```xml
<dependency>
    <groupId>com.alicp.jetcache</groupId>
    <artifactId>jetcache-anno</artifactId>
    <version>${jetcache.latest.version}</version>
</dependency>
<dependency>
    <groupId>com.alicp.jetcache</groupId>
    <artifactId>jetcache-redis</artifactId>
    <version>${jetcache.latest.version}</version>
</dependency>
```
配置了这个JetCacheConfig类以后，可以使用@CreateCache和@Cached注解。
```java
package com.company.mypackage;

import java.util.HashMap;
import java.util.Map;

import com.alicp.jetcache.anno.CacheConsts;
import com.alicp.jetcache.anno.config.EnableCreateCacheAnnotation;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import com.alicp.jetcache.anno.support.SpringConfigProvider;
import com.alicp.jetcache.embedded.EmbeddedCacheBuilder;
import com.alicp.jetcache.embedded.LinkedHashMapCacheBuilder;
import com.alicp.jetcache.redis.RedisCacheBuilder;
import com.alicp.jetcache.support.Fastjson2KeyConvertor;
import com.alicp.jetcache.support.JavaValueDecoder;
import com.alicp.jetcache.support.JavaValueEncoder;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.util.Pool;

@Configuration
@EnableMethodCache(basePackages = "com.company.mypackage")
@EnableCreateCacheAnnotation // deprecated in jetcache 2.7, 如果不用@CreateCache注解可以删除
@Import(JetCacheBaseBeans.class) //need since jetcache 2.7+
public class JetCacheConfig {

    @Bean
    public Pool<Jedis> pool(){
        GenericObjectPoolConfig pc = new GenericObjectPoolConfig();
        pc.setMinIdle(2);
        pc.setMaxIdle(10);
        pc.setMaxTotal(10);
        return new JedisPool(pc, "localhost", 6379);
    }

    //@Bean for jetcache <=2.6 
    //public SpringConfigProvider springConfigProvider() {
    //    return new SpringConfigProvider();
    //}

    @Bean
    public GlobalCacheConfig config(Pool<Jedis> pool){
    // public GlobalCacheConfig config(SpringConfigProvider configProvider, Pool<Jedis> pool){ // for jetcache <=2.5 
        Map localBuilders = new HashMap();
        EmbeddedCacheBuilder localBuilder = LinkedHashMapCacheBuilder
                .createLinkedHashMapCacheBuilder()
                .keyConvertor(FastjsonKeyConvertor.INSTANCE);
        localBuilders.put(CacheConsts.DEFAULT_AREA, localBuilder);

        Map remoteBuilders = new HashMap();
        RedisCacheBuilder remoteCacheBuilder = RedisCacheBuilder.createRedisCacheBuilder()
                .keyConvertor(Fastjson2KeyConvertor.INSTANCE)
                .valueEncoder(JavaValueEncoder.INSTANCE)
                .valueDecoder(JavaValueDecoder.INSTANCE)
                .broadcastChannel("projectA")
                .jedisPool(pool);
        remoteBuilders.put(CacheConsts.DEFAULT_AREA, remoteCacheBuilder);

        GlobalCacheConfig globalCacheConfig = new GlobalCacheConfig();
        // globalCacheConfig.setConfigProvider(configProvider); // for jetcache <= 2.5
        globalCacheConfig.setLocalCacheBuilders(localBuilders);
        globalCacheConfig.setRemoteCacheBuilders(remoteBuilders);
        globalCacheConfig.setStatIntervalMinutes(15);
        //globalCacheConfig.setAreaInCacheName(false); for jetcache <=2.6 

        return globalCacheConfig;
    }
}

```

# 进一步阅读
* 创建Cache实例的详细使用说明可以看[这里](CreateCache.md)
* Cache接口API使用可以看[这里](CacheAPI.md)
* 关于方法缓存(@Cached, @CacheUpdate, @CacheInvalidate)的详细使用看[这里](MethodCache.md)
* 详细的配置说明看[这里](Config.md)。
