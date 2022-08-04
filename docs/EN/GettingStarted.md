
# Create Cache instance
Create a ```Cache``` instance using @CreateCache annotation with default TTL 100 seconds.
```java
@CreateCache(expire = 100)
private Cache<Long, UserDO> userCache;
```
Then use ```Cache``` instance like a map:
```java
UserDO user = userCache.get(123L);
userCache.put(123L, user);
userCache.remove(123L);
```

Create Two level (local + remote, since cacheType = CacheType.BOTH) ```Cache``` instance, the limit of elements in local memory cache is 50: 
```java
@CreateCache(name = "UserService.userCache", expire = 100, cacheType = CacheType.BOTH, localLimit = 50)
private Cache<Long, UserDO> userCache;
```
Although name attribute is optional, it's good practice to naming each ```Cache``` instance. 
Statistics display will use the name for output, or auto generate one when name is absent. 
If two @CreateCache annotation with same name and cache area, they will point to same ```Cache``` instance.

# Create method cache
@Cached can be used to add method cache for a method. 
JetCache use Spring AOP to generate proxy to support method cache。
The @Cached annotation can be add on interface method or class method, but the interface or class must defined as a Spring bean.
```java
public interface UserService {
    @Cached(name="UserService.getUserById", expire = 3600)
    User getUserById(long userId);
}
```

# Basic configuration (using Spring Boot)
This example using jedis to accessing redis(or you can consider using [lettuce](RedisWithLettuce.md) client): 
# POM
```xml
<dependency>
    <groupId>com.alicp.jetcache</groupId>
    <artifactId>jetcache-starter-redis</artifactId>
    <version>${jetcache.latest.version}</version>
</dependency>
```
Create a Spring Boot style configuration file application.yml and put it into the resource dir. 
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
      keyConvertor: fastjson
      valueEncoder: java
      valueDecoder: java
      poolConfig:
        minIdle: 5
        maxIdle: 20
        maxTotal: 50
      host: 127.0.0.1
      port: 6379
```
Then create the application class of Spring Boot:
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
The @EnableMethodCache and @EnableCreateCacheAnnotation annotation activate @Cached and @CreateCache respectively.
Other code are same with standard Spring Boot Application. This class can run directly using main method.

# Basic configuration (without Spring Boot)
This example using jedis to accessing redis: 
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
The following code configure JetCache using hand coding, then you can use @CreateCache and @Cached in you beans.
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
import com.alicp.jetcache.support.FastjsonKeyConvertor;
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
@EnableCreateCacheAnnotation
public class JetCacheConfig {

    @Bean
    public Pool<Jedis> pool(){
        GenericObjectPoolConfig pc = new GenericObjectPoolConfig();
        pc.setMinIdle(2);
        pc.setMaxIdle(10);
        pc.setMaxTotal(10);
        return new JedisPool(pc, "localhost", 6379);
    }

    @Bean
    public SpringConfigProvider springConfigProvider() {
        return new SpringConfigProvider();
    }

    @Bean
    public GlobalCacheConfig config(Pool<Jedis> pool){
    // public GlobalCacheConfig config(SpringConfigProvider configProvider, Pool<Jedis> pool){ // for jetcache 2.5 
        Map localBuilders = new HashMap();
        EmbeddedCacheBuilder localBuilder = LinkedHashMapCacheBuilder
                .createLinkedHashMapCacheBuilder()
                .keyConvertor(FastjsonKeyConvertor.INSTANCE);
        localBuilders.put(CacheConsts.DEFAULT_AREA, localBuilder);

        Map remoteBuilders = new HashMap();
        RedisCacheBuilder remoteCacheBuilder = RedisCacheBuilder.createRedisCacheBuilder()
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .valueEncoder(JavaValueEncoder.INSTANCE)
                .valueDecoder(JavaValueDecoder.INSTANCE)
                .jedisPool(pool);
        remoteBuilders.put(CacheConsts.DEFAULT_AREA, remoteCacheBuilder);

        GlobalCacheConfig globalCacheConfig = new GlobalCacheConfig();
        // globalCacheConfig.setConfigProvider(configProvider); // for jetcache 2.5
        globalCacheConfig.setLocalCacheBuilders(localBuilders);
        globalCacheConfig.setRemoteCacheBuilders(remoteBuilders);
        globalCacheConfig.setStatIntervalMinutes(15);
        globalCacheConfig.setAreaInCacheName(false);

        return globalCacheConfig;
    }
}

```

# read more
* [Initiate ```Cache``` instance using @CreateCache annotation](CreateCache.md)
* [Basic Cache API](CacheAPI.md)
* [Method cache using annotation(@Cached, @CacheUpdate, @CacheInvalidate)](MethodCache.md)
* [Configuration details](Config.md)