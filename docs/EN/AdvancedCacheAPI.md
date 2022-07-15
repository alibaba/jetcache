# CacheBuilder
The ```CacheBuilder``` provide a way to construct ```Cache``` instance use your own code, See [Here](Builder.md).
```CacheBuilder``` are useful if you work without Spring, otherwise you may not use it directly.

# asynchronous API
All ```CacheResult``` returned by uppercase method support asynchronous access (since 2.2).
Presently asynchronous result only provide in redis-lettuce implementation.
Other implementations such as Tair, Jedis will block during uppercase method call.
But all the api are same.

For example:
```java
CacheGetResult<UserDO> r = cache.GET(userId);
```
If you use Tair, Jedis the ```GET``` operation will block until cache access finished.
However the ```GET``` method will return immediately if you use lettuce to access redis, 
any following call of r.isSuccess() or r.getValue() or r.getMessage() will block. To avoid this you can: 

```java
CompletionStage<ResultData> future = r.future();
future.thenRun(() -> {
    if(r.isSuccess()){
        System.out.println(r.getValue());
    }
});
```
The above code will execute callback in the thread which execute the asynchronous operation. 
```CompletionStage``` is a new feature in Java 8, read related documents if you are not familiar with it.
Note do not call any block method in the callback, since the callback is probably run in event-loop thread.

Some lowercase API can also get the advantage of asynchronous capability since they have no return value (in a asynchronous enabled backend such as lettuce).
For instance ```put``` and ```removeAll``` will return immediately, but ```get``` will not because ```get``` method need return a value.

# Auto load（read through）
```LoadingCache``` provide auto load capability (since 2.2). It is based on decorator pattern and implements ```Cache``` interface.
If a ```CacheBuilder``` has a loader, then its ```buildCache``` method will return a ```LoadingCache``` wrapper.
For example :
```java
Cache<Long,UserDO> userCache = LinkedHashMapCacheBuilder.createLinkedHashMapCacheBuilder()
                .loader(key -> loadUserFromDatabase(key))
                .buildCache();
```
The ```get``` and ```getAll``` method of ```LoadingCache``` will call the loader when cache miss. 
If any exception throws in the loader, ```get``` and ```getAll``` will throw ```CacheInvokeException```.

Notice:
1. uppercase method such as GET and GET_ALL only operate with cache and do not invoke the loader.
1. loader should be install on ```MultiLevelCache``` if you use multi level cache, do not install it on the cache underneath.

Unfortunately, we can't set the loader on ```@CreateCache``` because only constant is permit for the attribute of an annotation. 
So we use the follow code instead:
```java
@CreateCache
private Cache<Long,UserDO> userCache;

@PostConstruct
public void init(){
    userCache.config().setLoader(this::loadUserFromDatabase);
}
```
```@CreateCache``` always return a ```LoadingCache``` wrapper, so we can set a loader in the init method and take effect immediately.

# Auto refreshment
The ```RefreshCache``` provide auto refreshment capability (since 2.2). 
It is based on decorator pattern and implements ```Cache``` interface.
The ```buildCache``` method of ```CacheBuilder``` will return a ```RefreshCache``` wrapper when ```loader``` and ```refreshPolicy``` are both be set.

CacheBuilder usage: 
```java
RefreshPolicy policy = RefreshPolicy.newPolicy(1, TimeUnit.MINUTES)
                .stopRefreshAfterLastAccess(30, TimeUnit.MINUTES);
Cache<String, Long> orderSumCache = LinkedHashMapCacheBuilder
                .createLinkedHashMapCacheBuilder()
                .loader(key -> loadOrderSumFromDatabase(key))
                .refreshPolicy(policy)
                .buildCache();
```
The above code specify that every key-value pair should refresh every 1 minute since first access.
The refreshment of one key-value pair stops if this key does not be access in 30 minutes.
If the backend cache system is a remote cache (or ```MultiLevelCache``` with a remote cache as last layer), 
the refreshment is global exclusive, so it avoid two or more servers refresh same key concurrently (implements using ```tryLock``` in ```Cache```).

Similar with ```LoadingCache```, we init refresh policy in the init method when using with ```@CreateCache```:
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
