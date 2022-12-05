[![Java CI with Maven](https://github.com/alibaba/jetcache/actions/workflows/maven.yml/badge.svg)](https://github.com/alibaba/jetcache/actions/workflows/maven.yml)
[![Coverage Status](https://coveralls.io/repos/github/alibaba/jetcache/badge.svg?branch=master)](https://coveralls.io/github/alibaba/jetcache?branch=master)
[![GitHub release](https://img.shields.io/github/release/alibaba/jetcache.svg)](https://github.com/alibaba/jetcache/releases)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

# Introduction
JetCache is a Java cache abstraction which provides uniform usage for different caching solutions. 
It provides more powerful annotations than those in Spring Cache. The annotations in JetCache supports native TTL, 
two level caching, and automatically refresh in distrubuted environments, also you can manipulate ```Cache``` instance by your code. 
Currently, there are four implementations: ```RedisCache```, ```TairCache```(not open source on github), ```CaffeineCache``` (in memory) and a simple ```LinkedHashMapCache``` (in memory).
Full features of JetCache:
* Manipulate cache through uniform Cache API. 
* Declarative method caching using annotations with TTL(Time To Live) and two level caching support
* Create & configure ```Cache``` instance with cache manager
* Automatically collect access statistics for ```Cache``` instance and method level cache
* The strategy of key generation and value serialization can be customized
* Cache key convertor supported: ```fastjson```/```fastjson2```/```jackson```; Value convertor supported: ```java```/```kryo```/```kryo5```
* Distributed cache auto refresh and distributed lock. (2.2+)
* Asynchronous access using Cache API (2.2+, with redis lettuce client)
* Invalidate local caches (in all JVM process) after updates (2.7+)
* Spring Boot support

requirements:
* JDK1.8
* Spring Framework4.0.8+ (optional, with annotation support)，jetcache 2.7 need 5.2.4+
* Spring Boot1.1.9+ (optional), jetcache 2.7 need 2.2.5+

Visit [docs](docs/EN/Readme.md) for more details.

# Getting started

## Method cache
Declare method cache using ```@Cached``` annotation.  
```expire = 3600``` indicates that the elements will expire in 3600 seconds after being set.
JetCache automatically generates the cache key with all the parameters.
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
In order to use parameter name such as ```key="#userId"```, you javac compiler target must be 1.8 and above, and the ```-parameters``` should be set. Otherwise, use index to access parameters like ```key="args[0]"```

Auto refreshment:
```java
public interface SummaryService{
    @Cached(expire = 3600, cacheType = CacheType.REMOTE)
    @CacheRefresh(refresh = 1800, stopRefreshAfterLastAccess = 3600, timeUnit = TimeUnit.SECONDS)
    @CachePenetrationProtect
    BigDecimal summaryOfToday(long categoryId);
}
```
CachePenetrationProtect annotation indicates that the cache will be loaded synchronously in multi-thread environment.

## Cache API
Create a ```Cache``` instance with ```CacheManager```:

```java
@Autowired
private CacheManager cacheManager;
private Cache<String, UserDO> userCache;

@PostConstruct
public void init() {
    QuickConfig qc = QuickConfig.newBuilder("userCache")
        .expire(Duration.ofSeconds(100))
        .cacheType(CacheType.BOTH) // two level cache
        .localLimit(50)
        .syncLocal(true) // invalidate local cache in all jvm process after update
        .build();
    userCache = cacheManager.getOrCreateCache(qc);
}
```
The code above create a ```Cache``` instance. ```cacheType = CacheType.BOTH``` define a two level cache (a local in-memory-cache and a remote cache system) with local elements limited upper to 50(LRU based evict). You can use it like a map: 
```java
UserDO user = userCache.get(12345L);
userCache.put(12345L, loadUserFromDataBase(12345L));
userCache.remove(12345L);

userCache.computeIfAbsent(1234567L, (key) -> loadUserFromDataBase(1234567L));
```

## Advanced API
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

Read through and auto refresh:
```java
@Autowired
private CacheManager cacheManager;
private Cache<String, Long> orderSumCache;

@PostConstruct
public void init() {
    QuickConfig qc = QuickConfig.newBuilder("userCache")
        .expire(Duration.ofSeconds(3600))
        .loader(this::loadOrderSumFromDatabase)
        .refreshPolicy(RefreshPolicy.newPolicy(60, TimeUnit.SECONDS).stopRefreshAfterLastAccess(100, TimeUnit.SECONDS))
        .penetrationProtect(true)
        .build();
    orderSumCache = cacheManager.getOrCreateCache(qc);
}
```

## Configuration with Spring Boot

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
@EnableCreateCacheAnnotation // deprecated in jetcache 2.7, can be removed if @CreateCache is not used
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
      type: linkedhashmap #other choose：caffeine
      keyConvertor: fastjson2 #other choose：fastjson/jackson
      limit: 100
  remote:
    default:
      type: redis
      keyConvertor: fastjson2 #other choose：fastjson/jackson
      broadcastChannel: projectA
      valueEncoder: java #other choose：kryo/kryo5
      valueDecoder: java #other choose：kryo/kryo5
      poolConfig:
        minIdle: 5
        maxIdle: 20
        maxTotal: 50
      host: ${redis.host}
      port: ${redis.port}
```
> Visit [detail configuration](docs/EN/Config.md) for more instructions
## More docs
Visit [docs](docs/EN/Readme.md) for more details.
