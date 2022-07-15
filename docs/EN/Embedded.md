
The *keyConvertor* is optional if you are using Cache API in jetcache-core,
the local cache uses ```equals``` to identity the key. 
You must specify *keyConvertor* if you use annotations in jetcache-anno, such as @Cached and @CreateCache.

There are two local cache (class AbstractEmbeddedCache) implementation in JetCache.

# LinkedHashMapCache
```LinkedHashMapCache``` is a simple implementation in JetCache.
It is built on ```java.util.LinkedHashMap``` and supports LRU algorithm.
```java
Cache<Long, OrderDO> cache = LinkedHashMapCacheBuilder.createLinkedHashMapCacheBuilder()
                .limit(100)
                .expireAfterWrite(200, TimeUnit.SECONDS)
                .buildCache();
```

# CaffeineCache
CaffeineCache is built on [caffeine cache](https://github.com/ben-manes/caffeine).
```java
Cache<Long, OrderDO> cache = CaffeineCacheBuilder.createCaffeineCacheBuilder()
                .limit(100)
                .expireAfterWrite(200, TimeUnit.SECONDS)
                .buildCache();
```



