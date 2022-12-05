
Here is an example of yml config file in Spring Boot:
```
jetcache:
  statIntervalMinutes: 15
  areaInCacheName: false
  hidePackages: com.alibaba
  local:
    default:
      type: caffeine
      limit: 100
      keyConvertor: fastjson2 #other choose：fastjson/jackson
      expireAfterWriteInMillis: 100000
    otherArea:
      type: linkedhashmap
      limit: 100
      keyConvertor: none
      expireAfterWriteInMillis: 100000
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
    otherArea:
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
You can configure ```GlobalCacheConfig``` directly without Spring Boot. It's similar. See getting started tutorial.

The description of configuration listed in the below table:

| configuration key | default value | description                                                                                                                                                                                                                                                                                                                                          |
| --- | --- |------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| jetcache.statIntervalMinutes | 0 | Specify statistic interval, in minutes. 0 indicate no statistics.                                                                                                                                                                                                                                                                                    |
| jetcache.areaInCacheName | true(2.6-) false(2.7+) | jetcache-anno use *cache name* as remote cache key prefix, in jetcache 2.4.3 and previous version, it allways add *area name* in *cache name*. Since 2.4.4 we have this config item, for compatible reason default value is *true*. However *false* value are more reasonable for new project. 2.7 changes default value to false                    |
| jetcache.hiddenPackages | undefined | The package name startsWith(hiddenPackages) will be cut off in the generated cache instance name.                                                                                                                                                                                                                                                    |
| jetcache.[local/remote].${area}.type | undefined | Type of the backend cache system. Can be ```tair```, ```redis``` for remote cache ,or ```linkedhashmap```, ```caffeine``` for local cache.                                                                                                                                                                                                           |
| jetcache.[local/remote].${area}.keyConvertor | fastjson2 | Global config of key convertor. 2.6.5+ support key convertor: ```fastjson2```/```jackson```;</br>2.6.5- only build-in key convertor: ```fastjson```. You can use ```none``` only in the case of ```@CreateCache(cacheType=CacheType.LOCAL)```, in this situation ```equals``` is used to distinguish key. Method caching must specify a keyConvertor |
| jetcache.[local/remote].${area}.valueEncoder | java | Global config of value encoder, only remote cache need it. 2.7+ support valueEncoder: ```java```/```kryo```/```kryo5```；2.6- build-in valueEncoder: ```java```/```kryo```                                                                                                                                                                            |
| jetcache.[local/remote].${area}.valueDecoder | java | Global config of value decoder, only remote cache need it. 2.7+ support valueEncoder: ```java```/```kryo```/```kryo5```；2.6- build-in valueEncoder: ```java```/```kryo```                                                                                                                                                                            |
| jetcache.[local/remote].${area}.limit | 100 | Global config of max elements in local memory for *each* ```Cache``` instance. Only local cache need it.                                                                                                                                                                                                                                             |
| jetcache.[local/remote].${area}.expireAfterWriteInMillis | infinity | Global config of write expire time, in millis.                                                                                                                                                                                                                                                                                                       |
| jetcache.remote.${area}.broadcastChannel | n/a | jetcahe2.7 support invalidate local cache of other jvm after updatation (cacheType = CacheType.BOTH), this config specify broadcast channel, this feature disabled if not set                                                                                                                                                                        |
| jetcache.local.${area}.expireAfterAccessInMillis | 0 | Global config of read expire time, in millis. Need jetcache2.2+, only local cache support this feature. 0 indicates disabled read expire feature.                                                                                                                                                                                                    |

The ${area} of the above table is the ```area``` attribute of ```@Cached``` and ```@CreateCache```. Note that the default value of ```area``` attribute of the two annotation is ```"default"```.

There are multi place which the write expire time can be set:
1. if a method like ```put``` in ```Cache``` interface sets expire, then use it.
1. if not set in method like ```put```, use default expire of the ```Cache``` instance
1. the default expire of the ```Cache``` instance can be set in attribute on ```@CreateCache``` or ```@Cached```, if not, JetCache use global config ```defaultExpireInMillis``` defined in yml(for instance ```@Cached(cacheType=local)``` use ```jetcache.local.default.expireAfterWriteInMillis```), if there is not defined yet then use infinity.
