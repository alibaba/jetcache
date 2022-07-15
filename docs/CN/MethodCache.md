
JetCache方法缓存和SpringCache比较类似，它原生提供了TTL支持，以保证最终一致，并且支持二级缓存。JetCache2.4以后支持基于注解的缓存更新和删除。

在spring环境下，使用@Cached注解可以为一个方法添加缓存，@CacheUpdate用于更新缓存，@CacheInvalidate用于移除缓存元素。注解可以加在接口上也可以加在类上，加注解的类必须是一个spring bean，例如：
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
key使用Spring的[SpEL](https://docs.spring.io/spring/docs/4.2.x/spring-framework-reference/html/expressions.html)脚本来指定。如果要使用参数名（比如这里的```key="#userId"```），项目编译设置target必须为1.8格式，并且指定javac的-parameters参数，否则就要使用```key="args[0]"```这样按下标访问的形式。

@CacheUpdate和@CacheInvalidate的name和area属性必须和@Cached相同，name属性还会用做cache的key前缀。


@Cached注解和@CreateCache的属性非常类似，但是多几个：

|属性|默认值|说明|
| --- | --- | --- |
|area|“default”|如果在配置中配置了多个缓存area，在这里指定使用哪个area|
|name|未定义|指定缓存的唯一名称，不是必须的，如果没有指定，会使用类名+方法名。name会被用于远程缓存的key前缀。另外在统计中，一个简短有意义的名字会提高可读性。|
|key|未定义|使用[SpEL](https://docs.spring.io/spring/docs/4.2.x/spring-framework-reference/html/expressions.html)指定key，如果没有指定会根据所有参数自动生成。|
|expire|未定义| 超时时间。如果注解上没有定义，会使用全局配置，如果此时全局配置也没有定义，则为无穷大|
|timeUnit|TimeUnit.SECONDS|指定expire的单位|
|cacheType|CacheType.REMOTE|缓存的类型，包括CacheType.REMOTE、CacheType.LOCAL、CacheType.BOTH。如果定义为BOTH，会使用LOCAL和REMOTE组合成两级缓存|
|localLimit|未定义|如果cacheType为LOCAL或BOTH，这个参数指定本地缓存的最大元素数量，以控制内存占用。如果注解上没有定义，会使用全局配置，如果此时全局配置也没有定义，则为100|
|localExpire|未定义|仅当cacheType为BOTH时适用，为内存中的Cache指定一个不一样的超时时间，通常应该小于expire|
|serialPolicy|未定义|指定远程缓存的序列化方式。可选值为SerialPolicy.JAVA和SerialPolicy.KRYO。如果注解上没有定义，会使用全局配置，如果此时全局配置也没有定义，则为SerialPolicy.JAVA|
|keyConvertor|未定义|指定KEY的转换方式，用于将复杂的KEY类型转换为缓存实现可以接受的类型，当前支持KeyConvertor.FASTJSON和KeyConvertor.NONE。NONE表示不转换，FASTJSON可以将复杂对象KEY转换成String。如果注解上没有定义，会使用全局配置。|
|enabled|true|是否激活缓存。例如某个dao方法上加缓存注解，由于某些调用场景下不能有缓存，所以可以设置enabled为false，正常调用不会使用缓存，在需要的地方可使用CacheContext.enableCache在回调中激活缓存，缓存激活的标记在ThreadLocal上，该标记被设置后，所有enable=false的缓存都被激活|
|cacheNullValue|false|当方法返回值为null的时候是否要缓存|
|condition|未定义|使用[SpEL](https://docs.spring.io/spring/docs/4.2.x/spring-framework-reference/html/expressions.html)指定条件，如果表达式返回true的时候才去缓存中查询|
|postCondition|未定义|使用[SpEL](https://docs.spring.io/spring/docs/4.2.x/spring-framework-reference/html/expressions.html)指定条件，如果表达式返回true的时候才更新缓存，该评估在方法执行后进行，因此可以访问到#result|

@CacheInvalidate注解说明：

|属性|默认值|说明|
| --- | --- | --- |
|area|“default”|如果在配置中配置了多个缓存area，在这里指定使用哪个area，指向对应的@Cached定义。|
|name|未定义|指定缓存的唯一名称，指向对应的@Cached定义。|
|key|未定义|使用[SpEL](https://docs.spring.io/spring/docs/4.2.x/spring-framework-reference/html/expressions.html)指定key|
|condition|未定义|使用[SpEL](https://docs.spring.io/spring/docs/4.2.x/spring-framework-reference/html/expressions.html)指定条件，如果表达式返回true才执行删除，可访问方法结果#result|

@CacheUpdate注解说明：

|属性|默认值|说明|
| --- | --- | --- |
|area|“default”|如果在配置中配置了多个缓存area，在这里指定使用哪个area，指向对应的@Cached定义。|
|name|未定义|指定缓存的唯一名称，指向对应的@Cached定义。|
|key|未定义|使用[SpEL](https://docs.spring.io/spring/docs/4.2.x/spring-framework-reference/html/expressions.html)指定key|
|value|未定义|使用[SpEL](https://docs.spring.io/spring/docs/4.2.x/spring-framework-reference/html/expressions.html)指定value|
|condition|未定义|使用[SpEL](https://docs.spring.io/spring/docs/4.2.x/spring-framework-reference/html/expressions.html)指定条件，如果表达式返回true才执行更新，可访问方法结果#result|

使用@CacheUpdate和@CacheInvalidate的时候，相关的缓存操作可能会失败（比如网络IO错误），所以指定缓存的超时时间是非常重要的。


@CacheRefresh注解说明：

|属性|默认值|说明|
| --- | --- | --- |
|refresh|未定义|刷新间隔|
|timeUnit|TimeUnit.SECONDS|时间单位|
|stopRefreshAfterLastAccess|未定义|指定该key多长时间没有访问就停止刷新，如果不指定会一直刷新|
|refreshLockTimeout|60秒|类型为BOTH/REMOTE的缓存刷新时，同时只会有一台服务器在刷新，这台服务器会在远程缓存放置一个分布式锁，此配置指定该锁的超时时间|

@CachePenetrationProtect注解：

当缓存访问未命中的情况下，对并发进行的加载行为进行保护。
当前版本实现的是单JVM内的保护，即同一个JVM中同一个key只有一个线程去加载，其它线程等待结果。


对于以上未定义默认值的参数，如果没有指定，将使用yml中指定的全局配置，全局配置请参考[配置说明](Config.md)。

