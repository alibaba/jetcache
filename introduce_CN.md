阿里巴巴开源的通用缓存访问框架JetCache介绍

[JetCache](https://github.com/alibaba/jetcache)是由阿里巴巴开源的通用缓存访问框架，如果你对Spring Cache很熟悉的话，请一定花一点时间了解一下JetCache，它更好用。

JetCache提供的核心能力包括：
* 提供统一的，类似jsr-107风格的API访问Cache，并可通过注解创建并配置Cache实例
* 通过注解实现声明式的方法缓存，支持TTL和两级缓存
* 分布式缓存自动刷新，分布式锁 (2.2+)
* 分布式多级缓存场景，缓存更新后，自动让所有的local cache失效（2.7+）
* 支持异步Cache API
* Spring Boot支持
* Key的生成策略和Value的序列化策略是可以定制的
* 针对所有Cache实例和方法缓存的自动统计

我们直接看代码，最简单的使用场景是这样的：
```java
public interface UserService {
    @Cached(expire = 3600, cacheType = CacheType.REMOTE)
    User getUserById(long userId);
}
```
这和Spring Cache很像，不过@Cached注解原生支持了TTL（超时时间），cacheType有LOCAL/REMOTE/BOTH三种选择，
分别代表本地内存/远程Cache Server（例如Redis）/两级缓存，可根据情况选用，合理的使用LOCAL或BOTH类型可以降低Cache Server的压力以及我们提供的服务的响应时间。

再看个复杂点的例子：
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
第一个例子中我们没有指定key，JetCache会根据参数自动生成，这个例子我们指定了key，并且展示了缓存的更新和删除。

自动刷新和加载保护是JetCache的大杀器，对于加载开销比较大的对象，为了防止缓存未命中时的高并发访问打爆数据库：
```java
public interface SummaryService{
    @Cached(expire = 3600, cacheType = CacheType.REMOTE)
    @CacheRefresh(refresh = 1800, stopRefreshAfterLastAccess = 3600, timeUnit = TimeUnit.SECONDS)
    @CachePenetrationProtect
    BigDecimal salesVolumeSummary(int timeId, long catagoryId);
}
```
cacheType为REMOTE或者BOTH的时候，刷新行为是全局唯一的，也就是说，即使应用服务器是一个集群，也不会出现多个服务器同时去刷新一个key的情况。
CachePenetrationProtect注解保证当缓存未命中的时候，一个JVM里面只有一个线程去执行方法，其它线程等待结果。
一个key的刷新任务，自该key首次被访问后初始化，如果该key长时间不被访问，在stopRefreshAfterLastAccess指定的时间后，相关的刷新任务就会被自动移除，这样就避免了浪费资源去进行没有意义的刷新。

加在方法上的注解毕竟不能提供最灵活的控制，所以JetCache提供了Cache API，使用起来就像Map一样：
```java
UserDO user = userCache.get(12345L);
userCache.put(12345L, loadUserFromDataBase(12345L));
userCache.remove(12345L);

userCache.computeIfAbsent(1234567L, (key) -> loadUserFromDataBase(1234567L));
```

Cache实例可以通过CacheManager创建（在2.7版本中CreateCache注解已经废弃），相同area和name的情况下，和@Cached注解共用同一个Cache实例：
```java
@Autowired
private CacheManager cacheManager;
private Cache<String, UserDO> userCache;

@PostConstruct
public void init() {
    QuickConfig qc = QuickConfig.newBuilder("userCache")
        .expire(Duration.ofSeconds(100))
        .cacheType(CacheType.BOTH) // two level cache
        .syncLocal(true) // invalidate local cache in all jvm process after update
        .build();
    userCache = cacheManager.getOrCreateCache(qc);
}
```

也可以通过和guava cache/caffeine类似的builder来创建（这是low level api）：
```java
GenericObjectPoolConfig pc = new GenericObjectPoolConfig();
pc.setMinIdle(2);
pc.setMaxIdle(10);
pc.setMaxTotal(10);
JedisPool pool = new JedisPool(pc, "127.0.0.1", 6379);
Cache<Long, UserDO> userCache = RedisCacheBuilder.createRedisCacheBuilder()
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .valueEncoder(JavaValueEncoder.INSTANCE)
                .valueDecoder(JavaValueDecoder.INSTANCE)
                .jedisPool(pool)
                .keyPrefix("userCache-")
                .expireAfterWrite(200, TimeUnit.SECONDS)
                .buildCache();
```

Cache接口支持异步：
```java
CacheGetResult r = cache.GET(userId);
CompletionStage<ResultData> future = r.future();
future.thenRun(() -> {
    if(r.isSuccess()){
        System.out.println(r.getValue());
    }
});
```

可以实现不严格的分布式锁：
```java
cache.tryLockAndRun("key", 60, TimeUnit.SECONDS, () -> heavyDatabaseOperation());
```

使用Cache实例也可以配置自动刷新：

```java
@PostConstruct
public void init() {
    QuickConfig qc = QuickConfig.newBuilder("userCache")
        .refreshPolicy(RefreshPolicy.newPolicy(60, TimeUnit.SECONDS))
        .build();
    userCache = cacheManager.getOrCreateCache(qc);
}
```

low level api的builder一样也可以做出自动刷新：
```java
Cache<String, Long> orderSumCache = RedisCacheBuilder.createRedisCacheBuilder()
    ......省略
    .refreshPolicy(RefreshPolicy.newPolicy(60, TimeUnit.SECONDS))
    .loader(this::loadOrderSumFromDatabase)
    .buildCache();
```

当前支持的缓存系统包括以下4个，而且要支持一种新的缓存也是非常容易的：
* [Caffeine](https://github.com/ben-manes/caffeine)（基于本地内存）
* LinkedHashMap（基于本地内存，JetCache自己实现的简易LRU缓存）
* Alibaba Tair（相关实现未在Github开源，在阿里内部Gitlab上可以找到）
* Redis（含jedis、lettuce、spring-data、redisson几种访问方式）


使用JetCache的系统需求：
* JDK：必须Java 8
* Spring Framework（可选，如果用low level api就不需要）：jetcache2.5需要4.0.8以上，jetcache2.7需要5.2.4以上
* Spring Boot（可选）：jetcache2.5需要1.1.9以上，jetcache2.7需要2.2.5以上

更多文档可以在github仓库的docs中找到。

有了JetCache，我们就可以更方便的基于统一的接口访问缓存。


