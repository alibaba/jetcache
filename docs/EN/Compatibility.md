# spring compatibility
jetcache tested with below spring/spring-boot versions

| jetcache | spring                      | spring boot                 | comments                                                                                                                 |
|----------|-----------------------------|-----------------------------|--------------------------------------------------------------------------------------------------------------------------|
| 2.5      | 4.0.8.RELEASE~5.1.1.RELEASE | 1.1.9.RELEASE~2.0.5.RELEASE ||
| 2.6      | 5.0.4.RELEASE~5.2.4.RELEASE | 2.0.0.RELEASE~2.2.5.RELEASE | jetcache-redis depends on jedis3.1.0, spring-data(jedis, boot version<=2.1.X) depends on jedis2.9.3, can't used together |
| 2.7      | 5.2.4.RELEASE~5.3.23        | 2.2.5.RELEASE~2.7.5         | jetcache-redis depends on jedis4, spring-data(jedis) depends on jedis3, can't used together                              |
| 2.7.4      | 5.2.4.RELEASE~6.2.18        | 2.2.5.RELEASE~3.5.14        | can also support Spring 7/Spring Boot 4, but the BOM defines Spring 6/Spring Boot 3 by default |
| 2.8        | 6.x~7.0.7                   | 3.x~4.0.6                   | requires Java 17+; BOM defaults to Spring Framework 7.0.7 / Spring Boot 4.0.6 / Spring Data Redis 4.0.5 / SLF4J 2.x |

# compatible change notes
## 2.8.0
* Java 17 is now the minimum required version
* `areaInCacheName` default value is now `false` (was `true` in versions prior to 2.8.0).
* kryo4 is no longer supported, `com.esotericsoftware:kryo` is upgraded to 5.x. The `KRYO` constant in `SerialPolicy` now uses kryo5 implementation internally. kryo4 serialized data is not compatible with kryo5, wait for old cache entries to expire or clear cache before upgrading
* Removed fastjson1 support, `fastjson` key convertor now uses fastjson2 internally. If you need fastjson1, add the dependency yourself and implement a custom KeyConvertor
* Removed Spring XML namespace support (`<jetcache:xxx>` tags in XML configuration are no longer available)
* Added deserialization filter mechanism (enabled by default). This is a **breaking change** — if your cached values contain custom classes not in the default allowed list, deserialization (or serialization) will fail immediately after upgrading.
  
  **Upgrade steps**: Since older versions do not have this configuration option, pre-configuration before upgrading is not possible. Two recommended approaches:
  
  Option 1: Add `decodeFilterAllowPatterns` configuration **at the same time** as upgrading JetCache, including the package names of your custom classes in the allow list. For example:
  ```yaml
  jetcache:
    decodeFilterAllowPatterns:
      - com.yourcompany.
  ```
  
  Option 2: Disable the filter during upgrade (same behavior as 2.7):
  ```yaml
  jetcache:
    decodeFilterEnabled: false
  ```
  
  See the "Deserialization Filter Configuration" section in the [configuration docs](Config.md) for the list of default allowed packages and detailed setup instructions. 

## 2.7.4
* use spring-boot 3.1.3, spring-framework 6.0.11, slf4j-api 2.x as default
* remove javax.annotation:javax.annotation-api, if you use @PostConstruct, you may need to add this dependency by yourself

## 2.7.2
* update encoder/decoder of redisson, not compatible with 2.7.1

## 2.7.0
* jetcache-redis depends on jedis4，springdata(jedis) depends on jedis3, can't use together
* encoder/decoder now support kryo4 and kryo5, in yml "kryo" is kryo4，"kryo5" is kryo5. the kryo4 and kryo5 is not compatible.
    * in maven kryo4 is com.esotericsoftware:kryo, kryo5 is com.esotericsoftware.kryo:kryo5
    * kryo4 and kryo5 can be used together
    * notice that version of com.esotericsoftware:kryo can be set to 5.x.x
* use lettuce to connect redis cluster need specify "mode=cluster" in yml
* default key convertor change to "fastjson2", fastjson2 and fastjson can be used together, fastjson(not fastjson2)/kryo/kryo5/mvel is now optional in maven
* if not use spring boot, add ```@Import(JetCacheBaseBeans.class)```, and remove old configProvider bean definition. see docs for detail example.
* change GlobalCacheConfig.areaInCacheName default value to false (has bug, default value may still be true), need to add areaInCacheName=false

## 2.6.0
* GET/GET_ALL method of RefreshCache will not trigger auto refresh
* lettuce 4 is not supported
* jedis 2.9 is not supported
## 2.5.0
* ClassCastException may occurs when upgrade directly from versions <=2.3.3 and MultiLevelCache(or cacheType=CacheType.BOTH) is used. To solve this problem, upgrade to 2.4.4 and deploy it to product env first, then upgrade to 2.5.0 or above.
* Annotations on sub classes will override annotations on interfaces and super class.
