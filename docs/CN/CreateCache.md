
# CacheManager
使用CacheManager可以创建Cache实例，area和name相同的情况下，它和Cached注解使用同一个Cache实例。

*注意：在jetcache 2.7 版本CreateCache注解已经废弃，请改用CacheManager.getOrCreateCache(QuickConfig)*

例子：
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


# CreateCache注解

使用@CreateCache注解创建一个Cache实例，例如
```java
@CreateCache(expire = 100)
private Cache<Long, UserDO> userCache;
```

## @CreateCache属性表

|属性|默认值|说明|
| --- | --- | --- |
|area|“default”|如果需要连接多个缓存系统，可在配置多个cache area，这个属性指定要使用的那个area的name|
|name|未定义|指定缓存的名称，不是必须的，如果没有指定，会使用类名+方法名。name会被用于远程缓存的key前缀。另外在统计中，一个简短有意义的名字会提高可读性。如果两个```@CreateCache```的```name```和```area```相同，它们会指向同一个```Cache```实例|
|expire|未定义|该Cache实例的默认超时时间定义，注解上没有定义的时候会使用全局配置，如果此时全局配置也没有定义，则取无穷大|
|timeUnit|TimeUnit.SECONDS|指定expire的单位|
|cacheType|CacheType.REMOTE|缓存的类型，包括CacheType.REMOTE、CacheType.LOCAL、CacheType.BOTH。如果定义为BOTH，会使用LOCAL和REMOTE组合成两级缓存|
|localLimit|未定义|如果cacheType为CacheType.LOCAL或CacheType.BOTH，这个参数指定本地缓存的最大元素数量，以控制内存占用。注解上没有定义的时候会使用全局配置，如果此时全局配置也没有定义，则取100|
|serialPolicy|未定义|如果cacheType为CacheType.REMOTE或CacheType.BOTH，指定远程缓存的序列化方式。JetCache内置的可选值为SerialPolicy.JAVA和SerialPolicy.KRYO。注解上没有定义的时候会使用全局配置，如果此时全局配置也没有定义，则取SerialPolicy.JAVA|
|keyConvertor|未定义|指定KEY的转换方式，用于将复杂的KEY类型转换为缓存实现可以接受的类型，JetCache内置的可选值为KeyConvertor.FASTJSON和KeyConvertor.NONE。NONE表示不转换，FASTJSON通过fastjson将复杂对象KEY转换成String。如果注解上没有定义，则使用全局配置。|

## 默认值

对于以上未定义默认值的参数，如果没有指定，将使用yml中指定的全局配置，请参考[配置说明](Config.md)。