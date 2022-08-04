
The method caching in JetCache is similar like Spring Cache. JetCache provide naitve TTL support and two level cache support. 

Add ```@Cached``` on a method of a Spring bean to enable method caching. Since 2.4 ```@CacheUpdate``` and ```@CacheInvalidate``` are introduced for removing and updating method cache. You can even add the annotation on the interface that the bean implements.
```java
public interface UserService {
    @Cached(name="userCache.", key="#userId", expire = 3600)
    User getUserById(long userId);

    @CacheUpdate(name="userCache.", key="#user.userId", value="#user")
    void updateUser(User user);

    @CacheInvalidate(name="userCache.", key="#userId")
    void deleteUser(long userId);
}
```
the ```key``` and ```value``` attribute use [SpEL](https://docs.spring.io/spring/docs/4.2.x/spring-framework-reference/html/expressions.html) script. To enable use parameter name such as ```key="#userId"```, you javac compiler target must be 1.8 and the ```-parameters``` should be set, otherwise use index to access parameters like ```key="args[0]"```.

The attributes of ```@Cached``` are similar with ```@CreateCache``` except ```@Cached``` has more attributes:

|attribute|default value|description|
| --- | --- | --- |
|area|“default”|If you want to use multi backend cache system, you can setup multi "cache area" in configuration, this attribute specifies the name of the "cache area" you want to use.|
|name|undefined|The unique name of this ```Cache``` instance in an ```area```, optional. If you do not specify, JetCache will auto generate one. The name is used to display statistics information and as part of key prefix when using a remote cache. |
|key|undefined|use [SpEL](https://docs.spring.io/spring/docs/4.2.x/spring-framework-reference/html/expressions.html) script to specify the key. If not specified, JetCache will auto generate one using all method parameters.|
|expire|undefined|The expire time. Use global config if the attribute value is absent, and if the global config is not defined either, use infinity instead.|
|timeUnit|TimeUnit.SECONDS|Specify the time unit of ```expire```|
|cacheType|CacheType.REMOTE|Type of the ```Cache``` instance. May be CacheType.REMOTE, CacheType.LOCAL, CacheType.BOTH. Create a two level cache (local+remote) when value is CacheType.BOTH.|
|localLimit|undefined|Specify max elements in local memory when ```cacheType``` is CacheType.LOCAL or CacheType.BOTH. Use global config if the attribute value is absent, and if the global config is not defined either, use 100 instead.|
|localExpire|undefined|Only use with cacheType=CacheType.BOTH, specify a different local expire (typically less than expire) for local cache|
|serialPolicy|undefined|Specify the serialization policy of remote cache when ```cacheType``` is CacheType.REMOTE or CacheType.BOTH. The JetCache build-in ```serialPolicy``` are SerialPolicy.JAVA or SerialPolicy.KRYO. Use global config if the attribute value is absent, and if the global config is not defined either, use SerialPolicy.JAVA instead.|
|keyConvertor|undefined|Specify the key convertor. Used to convert the complex key object. The JetCache build-in ```keyConvertor``` are KeyConvertor.FASTJSON or KeyConvertor.NONE. NONE indicate do not convert, FASTJSON will use fastjson to convert key object to a string. Use global config if the attribute value is absent.|
|enabled|true|Specify whether the method caching is enabled. If set to false, you can enable it in thread context using ```CacheContext.enableCache(Supplier<T> callback)```|
|cacheNullValue|false|Specify whether a null value should be cached.|
|condition|undefined|Expression script used for conditioning the method caching, the cache is not used when evaluation result is false. Can't refer return value of real method.|
|postCondition|undefined|Expression script used for conditioning the method cache updating, the cache updating action is vetoed when the evaluation result is false. Evaluation occurs after real method invocation so we can refer *#result* in script.|

@CacheInvalidate attribute table:

|attribute|default value|description|
| --- | --- | --- |
|area|“default”|If you want to use multi backend cache system, you can setup multi "cache area" in configuration, this attribute specifies the name of the "cache area" you want to use.|
|name|undefined|The unique name of this ```Cache``` instance in an ```area```. refer to ```name``` of @Cached. |
|key|undefined|use [SpEL](https://docs.spring.io/spring/docs/4.2.x/spring-framework-reference/html/expressions.html) script to specify the key.|
|condition|undefined|Expression script used for conditioning the cache operation, the operation is vetoed when evaluation result is false. Evaluation occurs after real method invocation so we can refer *#result* in script.|

@CacheUpdate attribute table:

|attribute|default value|description|
| --- | --- | --- |
|area|“default”|If you want to use multi backend cache system, you can setup multi "cache area" in configuration, this attribute specifies the name of the "cache area" you want to use.|
|name|undefined|The unique name of this ```Cache``` instance in an ```area```. refer to ```name``` of @Cached. |
|key|undefined|use [SpEL](https://docs.spring.io/spring/docs/4.2.x/spring-framework-reference/html/expressions.html) script to specify the key.|
|value|undefined|use [SpEL](https://docs.spring.io/spring/docs/4.2.x/spring-framework-reference/html/expressions.html) script to specify the value.|
|condition|undefined|Expression script used for conditioning the cache operation, the operation is vetoed when evaluation result is false. Evaluation occurs after real method invocation so we can refer *#result* in script.|

Note that remote cache operation related to @CacheUpdate and @CacheInvalidate may fail, so it is important to set the '''expire''' attribute.

@CacheRefresh attribute table:

|attribute|default value|description|
| --- | --- | --- |
|refresh|undefined|interval of refreshment|
|timeUnit|TimeUnit.SECONDS|time unit|
|stopRefreshAfterLastAccess|undefined|if specified, refresh action will stop if the associated key is not accessed after specified time unit|
|refreshLockTimeout|60 seconds| the distributed lock timeout when cacheType is REMOTE or BOTH|

@CachePenetrationProtect:

This annotation used to synchronize concurrent cache loading operation. 
Currently it only take effect only in each single JVM, that is, in one JVM there is only one thread load for same key, 
other threads wait for the result. 

There are some attributes in the above table has no default value. JetCache will use global config when you not specify the value in annotation.
See [Configuration details](Config.md) for more information about global config.