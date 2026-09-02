
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
      keyConvertor: fastjson2 #other choose：fastjson(same as fastjson2)/jackson/jackson3
      expireAfterWriteInMillis: 100000
    otherArea:
      type: linkedhashmap
      limit: 100
      keyConvertor: none
      expireAfterWriteInMillis: 100000
  remote:
    default:
      type: redis
      keyConvertor: fastjson2 #other choose：fastjson(same as fastjson2)/jackson/jackson3
      broadcastChannel: projectA
      valueEncoder: java #other choose：kryo/kryo5
      valueDecoder: java #other choose：kryo/kryo5
      # Demonstrates both resolution paths: Spring bean via "bean:" and customContainer entry via plain name.
      externalWriteInterceptors: bean:loggingExternalCacheWriteInterceptor,auditExternalCacheWriteInterceptor
      poolConfig:
        minIdle: 5
        maxIdle: 20
        maxTotal: 50
      host: ${redis.host}
      port: ${redis.port}
    otherArea:
      type: redis
      keyConvertor: fastjson2 #other choose：fastjson(same as fastjson2)/jackson/jackson3
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
| jetcache.useDefaultLocalExpireInMultiLevelCache | false | If set to true, when cacheType is BOTH and localExpire is not explicitly set (including `@Cached`, `@CreateCache` annotations and `QuickConfig` API), the local cache expire time will be the minimum of the local cache builder's `expireAfterWriteInMillis` and `expire`. |
| jetcache.hiddenPackages | undefined | The package name startsWith(hiddenPackages) will be cut off in the generated cache instance name.                                                                                                                                                                                                                                                    |
| jetcache.[local/remote].${area}.type | undefined | Type of the backend cache system. Can be ```tair```, ```redis``` for remote cache ,or ```linkedhashmap```, ```caffeine``` for local cache.                                                                                                                                                                                                           |
| jetcache.[local/remote].${area}.keyConvertor | fastjson2 | Global config of key convertor. 2.8+ supports key convertor: ```fastjson2```/```jackson```/```jackson3``` (```fastjson``` is also available, which uses fastjson2 internally). You can use ```none``` only in the case of ```@CreateCache(cacheType=CacheType.LOCAL)```, in this situation ```equals``` is used to distinguish key. Method caching must specify a keyConvertor |
| jetcache.[local/remote].${area}.valueEncoder | java | Global config of value encoder, only remote cache need it. 2.8+ supports valueEncoder: ```java```/```kryo```/```kryo5``` (```kryo``` and ```kryo5``` both use kryo5 implementation) |
| jetcache.[local/remote].${area}.valueDecoder | java | Global config of value decoder, only remote cache need it. 2.8+ supports valueDecoder: ```java```/```kryo```/```kryo5``` (```kryo``` and ```kryo5``` both use kryo5 implementation) |
| jetcache.[local/remote].${area}.limit | 100 | Global config of max elements in local memory for *each* ```Cache``` instance. Only local cache need it.                                                                                                                                                                                                                                             |
| jetcache.[local/remote].${area}.expireAfterWriteInMillis | infinity | Global config of write expire time, in millis.                                                                                                                                                                                                                                                                                                       |
| jetcache.remote.${area}.broadcastChannel | n/a | jetcahe2.7 support invalidate local cache of other jvm after updatation (cacheType = CacheType.BOTH), this config specify broadcast channel, this feature disabled if not set                                                                                                                                                                        |
| jetcache.local.${area}.expireAfterAccessInMillis | 0 | Global config of read expire time, in millis. Need jetcache2.2+, only local cache support this feature. 0 indicates disabled read expire feature.                                                                                                                                                                                                    |
| jetcache.remote.${area}.externalWriteInterceptors | n/a | Specify external cache pre-write hooks, separated by commas. `bean:xxx` resolves a Spring Bean by name; a plain name resolves an entry from `AutoConfigureBeans.customContainer`. Example: `bean:loggingInterceptor,logging`. The hook receives read-only write metadata and is intended for logging, auditing, metrics or big-key detection, not for mutating the outgoing encoded write request. Returning `WriteInterceptDecision.reject(...)` rejects the current write operation through its cache result. Throwing an exception indicates the interceptor itself failed unexpectedly and is also reported through the cache result instead of propagating from the base write methods. Cache-specific configuration such as `@Cached`, `@CreateCache`, or `QuickConfig.externalWriteInterceptors(...)` overrides this global setting for the current cache. |
| jetcache.decodeFilterEnabled | true | Master switch for deserialization filter, enabled by default. Set to false to restore old behavior (NOT recommended) |
| jetcache.decodeFilterAllowPatterns | undefined | User-defined allow patterns appended to the default allow list. Three match modes are supported (see below) |
| jetcache.decodeFilterDenyPatterns | undefined | User-defined deny patterns appended to the default deny list. Deny patterns always take precedence over allow patterns |

The ${area} of the above table is the ```area``` attribute of ```@Cached``` and ```@CreateCache```. Note that the default value of ```area``` attribute of the two annotation is ```"default"```.

There are multi place which the write expire time can be set:
1. if a method like ```put``` in ```Cache``` interface sets expire, then use it.
1. if not set in method like ```put```, use default expire of the ```Cache``` instance
1. the default expire of the ```Cache``` instance can be set in attribute on ```@CreateCache``` or ```@Cached```, if not, JetCache use global config ```defaultExpireInMillis``` defined in yml(for instance ```@Cached(cacheType=local)``` use ```jetcache.local.default.expireAfterWriteInMillis```), if there is not defined yet then use infinity.

## Deserialization Filter Configuration

JetCache 2.8.x enables deserialization filter by default. The filter maintains both an **allow list** and a **deny list**. The deny list takes the highest priority and cannot be overridden by user-defined allow patterns.

The default allow list:

| Pattern | Match mode | Description |
| --- | --- | --- |
| `java.lang` | Package match | Direct classes only (e.g. String, Integer), excluding subpackages (reflect, invoke) |
| `java.util.` | Prefix match | Collections and subpackages (e.g. HashMap, concurrent.ConcurrentHashMap) |
| `java.time.` | Prefix match | Date/time classes (e.g. LocalDate, Duration) |
| `java.math` | Package match | BigDecimal, BigInteger, etc. (no subpackages exist) |
| `java.net` | Package match | URI, URL, etc. Direct classes only, excluding subpackages |
| `com.alicp.jetcache.` | Prefix match | JetCache internal classes |

If your cached values contain custom classes, you need to configure the filter:

```yaml
jetcache:
  decodeFilterEnabled: true  # default true, can set false to disable
  decodeFilterAllowPatterns:
    - com.example.          # prefix match: all classes under com.example and subpackages
    - org.myapp.dto         # package match: direct classes in org.myapp.dto (no subpackages)
    - org.myapp.dto.UserDTO # exact match: only this specific class
  decodeFilterDenyPatterns:
    - com.example.internal.      # block this package and its subpackages
    - org.myapp.dto.SecretDTO    # block one specific class
```

**Filter rules**: The built-in deny list includes known deserialization gadget chains (e.g. Commons Collections, Spring AOP, Hibernate, Groovy, JNDI/RMI, C3P0, etc.), dangerous classes like `java.lang.Runtime` and `ProcessBuilder`, and JDK internal packages like `com.sun.` and `sun.`. Deny patterns cannot be overridden by allow rules. If necessary, you can remove specific deny patterns via `DecodeFilter.getDefault().removeDenyPatterns(...)` (evaluate security risks yourself).

**Pattern matching rules**:
- **Prefix match** (ends with `.`): matches all classes in the package and all subpackages. For example, `com.example.` matches `com.example.Foo`, `com.example.sub.Bar`, etc.
- **Package match** (no trailing `.`, not a full class name): matches only classes directly in the package, excluding subpackages. For example, `com.example` matches `com.example.Foo` but not `com.example.sub.Bar`. The default allow list uses this mode for `java.lang` and `java.net`.
- **Exact match** (full class name): matches only one specific class. For example, `org.myapp.dto.UserDTO` matches only `org.myapp.dto.UserDTO`.

> **Tip**: If your custom classes are spread across multiple packages, prefix match (ending with `.`) is the most convenient option.

You can also configure programmatically (non-Spring Boot scenario):

```java
DecodeFilter filter = DecodeFilter.getDefault();
filter.addAllowPatterns("com.example.");
```

If a class is blocked during deserialization, an ERROR log is emitted (containing the rejected class name and configuration examples), and an exception is thrown. Kryo and JSON paths throw `DecodeFilterException`; Java serialization throws `InvalidClassException` (JDK internal behavior).

**Notes**:
- JDK dynamic proxy classes (e.g. `jdk.proxy1.$Proxy0`) are not in the default allow list. If you cache proxy objects (e.g. Spring AOP proxies), add an allow rule (e.g. `jdk.proxy.`).
- Packages like `java.rmi.`, `javax.naming.`, `java.lang.reflect.`, `javax.script.`, `javax.management.` are in the built-in deny list — adding allow rules cannot override them. Packages like `java.io`, `java.beans` (except `EventHandler`) are not in the allow list but also not in the deny list; they can be added via `decodeFilterAllowPatterns` or `addAllowPatterns`.
- If you need to block additional packages or classes beyond the built-in deny list, add them via `decodeFilterDenyPatterns` or `addDenyPatterns`.
