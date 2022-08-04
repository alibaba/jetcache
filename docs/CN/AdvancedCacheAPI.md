
# CacheBuilder
CacheBuilder提供使用代码直接构造Cache实例的方式，使用说明看[这里](Builder.md)。如果没有使用Spring，可以使用CacheBuilder，否则没有必要直接使用CacheBuilder。
# 异步API
从JetCache2.2版本开始，所有的大写API返回的CacheResult都支持异步。当底层的缓存实现支持异步的时候，大写API返回的结果都是异步的。当前支持异步的实现只有jetcache的redis-luttece实现，其他的缓存实现（内存中的、Tair、Jedis等），所有的异步接口都会同步堵塞，这样API仍然是兼容的。

以下的例子假设使用redis-luttece访问cache，例如：
```java
CacheGetResult<UserDO> r = cache.GET(userId);
```
这一行代码执行完以后，缓存操作可能还没有完成，如果此时调用r.isSuccess()或者r.getValue()或者r.getMessage()将会堵塞直到缓存操作完成。如果不想被堵塞，并且需要在缓存操作完成以后执行后续操作，可以这样做：
```java
CompletionStage<ResultData> future = r.future();
future.thenRun(() -> {
    if(r.isSuccess()){
        System.out.println(r.getValue());
    }
});
```
以上代码将会在缓存操作异步完成后，在完成异步操作的线程中调用thenRun中指定的回调。CompletionStage是Java8新增的功能，如果对此不太熟悉可以先查阅相关的文档。需要注意的是，既然已经选择了异步的开发方式，在回调中不能调用堵塞方法，以免堵塞其他的线程（回调方法很可能是在event loop线程中执行的）。

部分小写的api不需要任何修改，就可以直接享受到异步开发的好处。比如put和removeAll方法，由于它们没有返回值，所以此时就直接优化成异步调用，能够减少RT；而get方法由于需要取返回值，所以仍然会堵塞。
# 自动load（read through）
LoadingCache类提供了自动load的功能，它是一个包装，基于decorator模式，也实现了Cache接口。如果CacheBuilder指定了loader，那么buildCache返回的Cache实例就是经过LoadingCache包装过的。例如：
```java
Cache<Long,UserDO> userCache = LinkedHashMapCacheBuilder.createLinkedHashMapCacheBuilder()
                .loader(key -> loadUserFromDatabase(key))
                .buildCache();
```
LoadingCache的get和getAll方法，在缓存未命中的情况下，会调用loader，如果loader抛出异常，get和getAll会抛出CacheInvokeException。

需要注意
1. GET、GET_ALL这类大写API只纯粹访问缓存，不会调用loader。
1. 如果使用多级缓存，loader应该安装在MultiLevelCache上，不要安装在底下的缓存上。

注解的属性只能是常量，所以没有办法在CreateCache注解中指定loader，不过我们可以这样：
```java
@CreateCache
private Cache<Long,UserDO> userCache;

@PostConstruct
public void init(){
    userCache.config().setLoader(this::loadUserFromDatabase);
}
```
@CreateCache总是初始化一个经过LoadingCache包装的Cache，直接在config中设置loader，可以实时生效。

# 自动刷新缓存
从JetCache2.2版本开始，RefreshCache基于decorator模式提供了自动刷新的缓存的能力，目的是为了防止缓存失效时造成的雪崩效应打爆数据库。同时设置了loader和refreshPolicy的时候，CacheBuilder的buildCache方法返回的Cache实例经过了RefreshCache的包装。
```java
RefreshPolicy policy = RefreshPolicy.newPolicy(1, TimeUnit.MINUTES)
                .stopRefreshAfterLastAccess(30, TimeUnit.MINUTES);
Cache<String, Long> orderSumCache = LinkedHashMapCacheBuilder
                .createLinkedHashMapCacheBuilder()
                .loader(key -> loadOrderSumFromDatabase(key))
                .refreshPolicy(policy)
                .buildCache();
```
对一些key比较少，实时性要求不高，加载开销非常大的缓存场景，适合使用自动刷新。上面的代码指定每分钟刷新一次，30分钟如果没有访问就停止刷新。如果缓存是redis或者多级缓存最后一级是redis，缓存加载行为是全局唯一的，也就是说不管有多少台服务器，同时只有一个服务器在刷新，这是通过tryLock实现的，目的是为了降低后端的加载负担。

与LoadingCache一样，使用@CreateCache时，我们需要这样来添加自动刷新功能
```java
@CreateCache
private Cache<String, Long> orderSumCache;

@PostConstruct
public void init(){
    RefreshPolicy policy = RefreshPolicy.newPolicy(1, TimeUnit.MINUTES)
                          .stopRefreshAfterLastAccess(30, TimeUnit.MINUTES);
    orderSumCache.config().setLoader(this::loadOrderSumFromDatabase);
    orderSumCache.config().setRefreshPolicy(policy);
}
```
