[![Build Status](https://travis-ci.org/alibaba/jetcache.svg?branch=master)](https://travis-ci.org/alibaba/jetcache)
[![Coverage Status](https://coveralls.io/repos/alibaba/jetcache/badge.png?branch=master)](https://coveralls.io/r/alibaba/jetcache?branch=master) 
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.alicp.jetcache/jetcache-parent/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.alicp.jetcache/jetcache-parent/)
<!--[![GitHub release](https://img.shields.io/github/release/alibaba/jetcache.svg)](https://github.com/alibaba/jetcache/releases)-->
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

# introduction
JetCache is a Java cache abstraction which provides consistent use for various caching solutions. 
Presently it support Redis and Alibaba Tair and Caffeine cache as embedded cache.
It simplifies cache operation with these key features:  
* declarative caching using annotation with TTL(Time To Live) and two level caching support
* policy of key generation and value serialization can be custom
* detailed stat of each cache area
* consistent API
* Spring Boot support
* auto refresh cache (2.2+)
* asynchronous API (2.2+)

NOTICE: the Alibaba Tair support is not open source.

requirements:
* Java 8 is necessary.
* Spring Framework 4.0.8 or above. If you do not use declarative caching, it's not necessary.
* Spring Boot 1.1.9 or above, optional, only need if you are using Spring Boot.

Visit [wiki](https://github.com/alibaba/jetcache/wiki) for more documents (currently in Chinese only).

# getting started

## method cache
Declare method cache using ```Cached``` annotation.  ```expire = 3600``` indicates that the elements will expires in 3600 seconds after put.
```java
public interface UserService {
    @Cached(expire = 3600, cacheType = CacheType.REMOTE)
    User getUserById(long userId);
}
```

## cache API
Create a ```Cache``` instance using ```CreateCache``` annotation:
```java
@CreateCache(expire = 100, cacheType = CacheType.BOTH, localLimit = 50)
private Cache<Long, UserDO> userCache;
```
The code above create a ```Cache``` instance. ```cacheType = CacheType.BOTH``` define a two level cache (a local in-memory-cache and a remote cache system) with local elements limited upper to 50(LRU based evict). You can use it like a map: 
```java
UserDO user = userCache.get(12345L);
userCache.put(12345L, loadUserFromDataBase(12345L));
userCache.remove(12345L);

userCache.computeIfAbsent(1234567L, (key) -> loadUserFromDataBase(1234567L));
```
## configuration with Spring Boot

pom:
```xml
<dependency>
    <groupId>com.alicp.jetcache</groupId>
    <artifactId>jetcache-starter-redis</artifactId>
    <version>${jetcache.latest.version}</version>
</dependency>
```

App class:
```java
@SpringBootApplication
@EnableMethodCache(basePackages = "com.company.mypackage")
@EnableCreateCacheAnnotation
public class MySpringBootApp {
    public static void main(String[] args) {
        SpringApplication.run(MySpringBootApp.class);
    }
}
```

spring boot application.yml config:
```yaml
jetcache:
  statIntervalMinutes: 15
  local:
    default:
      type: linkedhashmap
      keyConvertor: fastjson
      limit: 100
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
      host: ${redis.host}
      port: ${redis.port}
```
## configuration without Spring Boot
pom:
```xml
<dependency>
    <groupId>com.alicp.jetcache</groupId>
    <artifactId>jetcache-anno</artifactId>
    <version>${jetcache.latest.version}</version>
</dependency>
```

config
```java
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
    public GlobalCacheConfig config(SpringConfigProvider configProvider, Pool<Jedis> pool){
        GlobalCacheConfig pc = new GlobalCacheConfig();

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
        globalCacheConfig.setConfigProvider(configProvider);
        globalCacheConfig.setLocalCacheBuilders(localBuilders);
        globalCacheConfig.setRemoteCacheBuilders(remoteBuilders);
        globalCacheConfig.setStatIntervalMinutes(15);

        return globalCacheConfig;
    }

}
```

## more docs
Visit [wiki](https://github.com/alibaba/jetcache/wiki) for more documents (currently in Chinese only).