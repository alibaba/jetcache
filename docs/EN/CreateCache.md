
# CacheManager
use CacheManager to create *Cache* instance, it returns same *Cache* instance with @Cached if *area* and *name* equals.

*notice: in jetcache 2.7 CreateCache annotation is deprecated, use CacheManager.getOrCreateCache(QuickConfig) instead*

example:
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

# CreateCache annotation

You can use ```@CreateCache``` annotation to create and configure a ```Cache``` instance in a Spring Bean.
For Example:
```java
@CreateCache(expire = 100)
private Cache<Long, UserDO> userCache;
```

# The attributes of @CreateCache

|attribute|default value|description|
| --- | --- | --- |
|area|“default”|If you want to use multi backend cache system, you can setup multi "cache area" in configuration, this attribute specifies the name of the "cache area" you want to use.|
|name|undefined|The name of this ```Cache``` instance, optional. If you do not specify, JetCache will auto generate one. The name is used to display statistics information and as part of key prefix when using a remote cache. If two ```@CreateCache``` have same ```name``` and ```area```, they will point to same ```Cache``` instance.|
|expire|undefined|The default expire time of this ```Cache``` instance. Use global config if the attribute value is absent, and if the global config is not defined either, use infinity.|
|timeUnit|TimeUnit.SECONDS|Specify the time unit of ```expire```|
|cacheType|CacheType.REMOTE|Type of the ```Cache``` instance. May be CacheType.REMOTE, CacheType.LOCAL, CacheType.BOTH. Use two level cache (local+remote) when value is CacheType.BOTH.|
|localLimit|undefined|Specify max elements in local memory when ```cacheType``` is CacheType.LOCAL or CacheType.BOTH. Use global config if the attribute value is absent, and if the global config is not defined either, use 100.|
|serialPolicy|undefined|Specify the serialization policy of remote cache when ```cacheType``` is CacheType.REMOTE or CacheType.BOTH. The JetCache build-in ```serialPolicy``` are SerialPolicy.JAVA or SerialPolicy.KRYO. Use global config if the attribute value is absent, and if the global config is not defined either, use ```SerialPolicy.JAVA```.|
|keyConvertor|undefined|Specify the key convertor. Used to convert the complex key object. The JetCache build-in ```keyConvertor``` are KeyConvertor.FASTJSON or KeyConvertor.NONE. NONE indicate do not convert, FASTJSON will use fastjson to convert key object to a string. Use global config if the attribute value is absent.|

# Default values
There are some attributes in the above table has no default value. JetCache will use global config value when you not specify the value in annotation.
See [Configuration details](Config.md) for more information about global config.