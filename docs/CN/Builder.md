
JetCache2版本的@Cached和@CreateCache等注解都是基于Spring4.X版本实现的，在没有Spring支持的情况下，注解将不能使用。但是可以直接使用JetCache的API来创建、管理、监控Cache，多级缓存也可以使用。

# 创建缓存
创建缓存的操作类似guava/caffeine cache，例如下面的代码创建基于内存的LinkedHashMapCache：
```java
Cache<String, Integer> cache = LinkedHashMapCacheBuilder.createLinkedHashMapCacheBuilder()
                .limit(100)
                .expireAfterWrite(200, TimeUnit.SECONDS)
                .buildCache();
```

创建RedisCache：
```java
GenericObjectPoolConfig pc = new GenericObjectPoolConfig();
        pc.setMinIdle(2);
        pc.setMaxIdle(10);
        pc.setMaxTotal(10);
        JedisPool pool = new JedisPool(pc, "localhost", 6379);
Cache<Long, OrderDO> orderCache = RedisCacheBuilder.createRedisCacheBuilder()
                .keyConvertor(Fastjson2KeyConvertor.INSTANCE)
                .valueEncoder(JavaValueEncoder.INSTANCE)
                .valueDecoder(JavaValueDecoder.INSTANCE)
                .jedisPool(pool)
                .keyPrefix("orderCache")
                .expireAfterWrite(200, TimeUnit.SECONDS)
                .buildCache();
```

# 多级缓存
在2.2以后通过下面的方式创建多级缓存：
```java
Cache multiLevelCache = MultiLevelCacheBuilder.createMultiLevelCacheBuilder()
      .addCache(memoryCache, redisCache)
      .expireAfterWrite(100, TimeUnit.SECONDS)
      .buildCache();
```
实际上，使用MultiLevelCache可以创建多级缓存，它的构造函数接收的是一个Cache数组（可变参数）。


# 监控统计
如果要对Cache进行监控统计：
```java
Cache orderCache = ...
CacheMonitor orderCacheMonitor = new DefaultCacheMonitor("OrderCache");
orderCache.config().getMonitors().add(orderCacheMonitor); // jetcache 2.2+, or call builder.addMonitor() before buildCache()
// Cache<Long, Order> monitedOrderCache = new MonitoredCache(orderCache, orderCacheMonitor); //before jetcache 2.2
int resetTime = 1;
boolean verboseLog = false;
DefaultCacheMonitorManager cacheMonitorManager = new DefaultCacheMonitorManager(resetTime, TimeUnit.SECONDS, verboseLog);
cacheMonitorManager.add(orderCacheMonitor);
cacheMonitorManager.start();
```
首先创建一个CacheMonitor，每个DefaultCacheMonitor只能用于一个Cache。当DefaultCacheMonitorManager启动以后，会使用slf4j按指定的时间定期输出统计信息到日志中（简版输出格式参见[统计](Stat.md)），DefaultCacheMonitor构造时指定的名字会作为输出时cache的名字。

在组装多级缓存的过程中，可以给每个缓存安装一个Monitor，这样可以监控每一级的命中情况。

也可以自己对统计信息进行处理，调用下面的构造方法创建DefaultCacheMonitorManager：
```java
public DefaultCacheMonitorManager(int resetTime, TimeUnit resetTimeUnit, Consumer<StatInfo> statCallback)
```