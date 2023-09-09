# spring compatibility
jetcache tested with below spring/spring-boot versions

| jetcache | spring                      | spring boot                 | comments                                                                                                                 |
|----------|-----------------------------|-----------------------------|--------------------------------------------------------------------------------------------------------------------------|
| 2.5      | 4.0.8.RELEASE~5.1.1.RELEASE | 1.1.9.RELEASE~2.0.5.RELEASE ||
| 2.6      | 5.0.4.RELEASE~5.2.4.RELEASE | 2.0.0.RELEASE~2.2.5.RELEASE | jetcache-redis depends on jedis3.1.0, spring-data(jedis, boot version<=2.1.X) depends on jedis2.9.3, can't used together |
| 2.7      | 5.2.4.RELEASE~5.3.23        | 2.2.5.RELEASE~2.7.5         | jetcache-redis depends on jedis4, spring-data(jedis) depends on jedis3, can't used together                              |
| 2.7.4      | 5.2.4.RELEASE~6.0.11        | 2.2.5.RELEASE~3.1.3         | |

# compatible change notes
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
* change GlobalCacheConfig.areaInCacheName default value to false

## 2.6.0
* GET/GET_ALL method of RefreshCache will not trigger auto refresh
* lettuce 4 is not supported
* jedis 2.9 is not supported
## 2.5.0
* ClassCastException may occurs when upgrade directly from versions <=2.3.3 and MultiLevelCache(or cacheType=CacheType.BOTH) is used. To solve this problem, upgrade to 2.4.4 and deploy it to product env first, then upgrade to 2.5.0 or above.
* Annotations on sub classes will override annotations on interfaces and super class.