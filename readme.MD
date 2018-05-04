[![Build Status](https://travis-ci.org/alibaba/jetcache.svg?branch=master)](https://travis-ci.org/alibaba/jetcache)
[![Coverage Status](https://coveralls.io/repos/github/alibaba/jetcache/badge.svg?branch=master)](https://coveralls.io/github/alibaba/jetcache?branch=master) 
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.alicp.jetcache/jetcache-parent/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.alicp.jetcache/jetcache-parent/)
[![GitHub release](https://img.shields.io/github/release/alibaba/jetcache.svg)](https://github.com/alibaba/jetcache/releases)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

# introduction
JetCache is a Java cache abstraction which provides consistent use for various caching solutions. 
It provides more powerful annotation than that in Spring Cache. The annotation in JetCache supports native TTL, 
two level caching, and distributed automatically refreshment, also you can operate ```Cache``` instance by hand code. 
Presently There are four implements: ```RedisCache```, ```TairCache```(not open source on github), ```CaffeineCache``` (in memory), a simple ```LinkedHashMapCache``` (in memory).
Full features of JetCache:
* Operate cache through consistent Cache API. 
* Declarative method caching using annotation with TTL(Time To Live) and two level caching support
* Create & configure ```Cache``` instance using annotation
* Automatically collect access statistics for ```Cache``` instance and method cache
* The policy of key generation and value serialization can be customized
* Distributed cache automatically refreshment and distributed lock. (2.2+)
* Asynchronous access using Cache API (2.2+, with redis lettuce client)
* Spring Boot support

requirements:
* JDK1.8
* Spring Framework4.0.8+ (optional, with annotation support)
* Spring Boot1.1.9+ (optional)

Visit [wiki](https://github.com/alibaba/jetcache/wiki) for more documents.

# getting started

## method cache
Declare method cache using ```@Cached``` annotation.  
```expire = 3600``` indicates that the elements will expires in 3600 seconds after put.
JetCache auto generates cache key using all parameters.
```java
public interface UserService {
    @Cached(expire = 3600, cacheType = CacheType.REMOTE)
    User getUserById(long userId);
}
```

Using ```key``` attribute to specify cache key using [SpEL](https://docs.spring.io/spring/docs/4.2.x/spring-framework-reference/html/expressions.html) script.
```java
public interface UserService {
    @Cached(name="userCache-", key="#userId", expire = 3600)
    User getUserById(long userId);

    @CacheUpdate(name="userCache-", key="#user.userId", value="#user")
    void updateUser(User user);

    @CacheInvalidate(name="userCache-", key="#userId")
    void deleteUser(long userId);
}
```
To enable use parameter name such as ```key="#userId"```, you javac compiler target must be 1.8 and the ```-parameters``` should be set, otherwise use index to access parameters like ```key="args[0]"```

Automatically refreshment:
```java
public interface SummaryService{
    @Cached(expire = 3600, cacheType = CacheType.REMOTE)
    @CacheRefresh(refresh = 1800, stopRefreshAfterLastAccess = 3600, timeUnit = TimeUnit.SECONDS)
    @CachePenetrationProtect
    BigDecimal summaryOfToday(long catagoryId);
}
```

## cache API
Create a ```Cache``` instance using ```@CreateCache``` annotation:
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

Or you can create ```Cache``` instance manually (RedisCache in this example) :
```java
GenericObjectPoolConfig pc = new GenericObjectPoolConfig();
pc.setMinIdle(2);
pc.setMaxIdle(10);
pc.setMaxTotal(10);
JedisPool pool = new JedisPool(pc, "localhost", 6379);
Cache<Long, UserDO> userCache = RedisCacheBuilder.createRedisCacheBuilder()
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .valueEncoder(JavaValueEncoder.INSTANCE)
                .valueDecoder(JavaValueDecoder.INSTANCE)
                .jedisPool(pool)
                .keyPrefix("userCache-")
                .expireAfterWrite(200, TimeUnit.SECONDS)
                .buildCache();
```

## advanced API
Asynchronous API:
```java
CacheGetResult r = cache.GET(userId);
CompletionStage<ResultData> future = r.future();
future.thenRun(() -> {
    if(r.isSuccess()){
        System.out.println(r.getValue());
    }
});
```

Distributed lock:
```java
cache.tryLockAndRun("key", 60, TimeUnit.SECONDS, () -> heavyDatabaseOperation());
```

Read through and automatically refreshment:
```java
@CreateCache
@CacheRefresh(timeUnit = TimeUnit.MINUTES, refresh = 60)
@CachePenetrationProtect
private Cache<String, Long> orderSumCache;

@PostConstruct
public void init(){
    orderSumCache.config().setLoader(this::loadOrderSumFromDatabase);
}
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
  areaInCacheName: false
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

## more docs
Visit [wiki](https://github.com/alibaba/jetcache/wiki) for more documents.