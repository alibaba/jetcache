
![JetCache logo](../images/logo_jetcache.png)

## Introduce
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

## Requirements
* JDK1.8
* Spring Framework4.0.8+ (optional, with annotation support)
* Spring Boot1.1.9+ (optional)

## samples
see *samples* directory of the repository.

## Documents
* [Getting Started](GettingStarted.md)
* [Basic Cache API](CacheAPI.md)
* [Initiate ```Cache``` instance using CacheManager](CreateCache.md)
* [Enable method caching using annotation](MethodCache.md)
* [Configuration details](Config.md)
* [Advanced Cache API](AdvancedCacheAPI.md)
* Redis support (using either of bellows)
  * [Using jedis client](RedisWithJedis.md)
  * [Using lettuce client](RedisWithLettuce.md)
  * Spring data redis (document needed)
* [In-memory ```Cache``` implements: LinkedHashMapCache and CaffeineCache](Embedded.md)
* [Statistics](Stat.md)
* [Builder API](Builder.md): If you do not use Spring Framework or want to construct ```Cache``` instance by hand coding
* [Compatibility notes](Compatibility.md) 

## which artifact should I use?
* jetcache-anno-api: define annotation and constants, no transitive dependencies. If you want to add JetCache annotation to your API artifact without introduce a lot of dependencies, you use jetcache-anno-api. 
* jetcache-core: core api, configuration ```Cache``` instance by hand coding. It also provide two in-memory implementations ```LinkedHashMapCache``` and ```CaffeineCache```. It does not depends on Spring.
* jetcache-anno: @CreateCache and @Cached annotation implements.
* jetcache-redis: redis support using jedis client.
* jetcache-redis-lettuce (JetCache2.3+): redis support using lettuce client, implements JetCache asynchronous API.
* jetcache-starter-redis: Spring Boot style starter for redis using jedis.
* jetcache-starter-redis-lettuce (JetCache2.3+): Spring Boot style starter for redis using lettuce.
