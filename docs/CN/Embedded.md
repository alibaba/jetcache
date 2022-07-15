
本地缓存当前有两个实现。如果自己用jetcache-core的Cache API，可以不指定keyConvertor，此时本地缓存使用equals方法来比较key。
如果使用jetcache-anno中的@Cached、@CreateCache等注解，必须指定keyConvertor。

# LinkedHashMapCache
LinkedHashMapCache是JetCache中实现的一个最简单的Cache，使用LinkedHashMap做LRU方式淘汰。
```java
Cache<Long, OrderDO> cache = LinkedHashMapCacheBuilder.createLinkedHashMapCacheBuilder()
                .limit(100)
                .expireAfterWrite(200, TimeUnit.SECONDS)
                .buildCache();
```

# CaffeineCache
caffeine cache的介绍看[这里](https://github.com/ben-manes/caffeine)，它是guava cache的后续作品。
```java
Cache<Long, OrderDO> cache = CaffeineCacheBuilder.createCaffeineCacheBuilder()
                .limit(100)
                .expireAfterWrite(200, TimeUnit.SECONDS)
                .buildCache();
```



