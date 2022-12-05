Annotations such as ```@Cached``` and ```@CreateCache``` are based on Spring Framework 4.0 or above.
You can use JetCache API to create, manage, monitor ```Cache``` instance.

# Create Cache
Similar to guava/caffeine cache. For example, the below code create a ```LinkedHashMapCache``` instance: 
```java
Cache<String, Integer> cache = LinkedHashMapCacheBuilder.createLinkedHashMapCacheBuilder()
                .limit(100)
                .expireAfterWrite(200, TimeUnit.SECONDS)
                .buildCache();
```

Create ```RedisCache``` (using jedis):
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

# Multi level cache
```java
Cache multiLevelCache = MultiLevelCacheBuilder.createMultiLevelCacheBuilder()
      .addCache(memoryCache, redisCache)
      .expireAfterWrite(100, TimeUnit.SECONDS)
      .buildCache();
```
You can even build cache more than two level.


# Monitor

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
Each ```CacheMonitor``` used for exactly one ```Cache``` instance.
After ```start```, ```DefaultCacheMonitorManager``` outputs statistics every ```resetTime``` using slf4j (see [Statistics](Stat)).

The constructor parameter of ```DefaultCacheMonitor``` will be used as cache name in output table.

You can add to ```CacheMonitor``` to every ```Cache``` instance in ```MultiLevelCache```,
so each cache in multi level cache is monitored.

The output format can be customized using this constructor:
```java
public DefaultCacheMonitorManager(int resetTime, TimeUnit resetTimeUnit, Consumer<StatInfo> statCallback)
```