
# Introduce
The core concept of JetCache is the ```com.alicp.jetcache.Cache```(hereinafter called  ```Cache```) interface, it provides some API similar like ```javax.cache.Cache``` in JSR107. 
The reason that JetCache does not implements JSR107 includes:
1. We want that the API of JetCache are more simpler and easy to use than JSR107.
1. Some operation defined in ```javax.cache.Cache``` are difficult to implement efficiently in some specific distributed cache system (eg. some atomic operation and ```removeAll()```). 
1. Implements whole JSR107 needs a lots of work.

# JSR107 style API
The follows methods in JetCache ```Cache``` interface are same with those in ```javax.cache.Cache``` except that
the methods in ```Cache``` never throws exception. 
```java
V get(K key)
void put(K key, V value);
boolean putIfAbsent(K key, V value); //MultiLevelCache do not support this method
boolean remove(K key);
<T> T unwrap(Class<T> clazz);
Map<K,V> getAll(Set<? extends K> keys);
void putAll(Map<? extends K,? extends V> map);
void removeAll(Set<? extends K> keys);
```

# JetCache Cache API
```java
V computeIfAbsent(K key, Function<K, V> loader)
V computeIfAbsent(K key, Function<K, V> loader, boolean cacheNullWhenLoaderReturnNull)
V computeIfAbsent(K key, Function<K, V> loader, boolean cacheNullWhenLoaderReturnNull, long expire, TimeUnit timeUnit)
```
If there is a value associated with the key, return the value, 
otherwise use the loader load the value and update the cache, then return the value.
The ```cacheNullWhenLoaderReturnNull``` parameter indicate whether null value returned by loader should put into cache.
The ```expire``` and ```timeUnit``` specifies the TTL (will overrides defaults) of the KV pair when update the cache.
Load time are recorded with these methods. 

```java
void put(K key, V value, long expire, TimeUnit timeUnit)
```
The put opperation. ```expire``` and ```timeUnit``` specifies the TTL (will overrides defaults).

```java
AutoReleaseLock tryLock(K key, long expire, TimeUnit timeUnit)
boolean tryLockAndRun(K key, long expire, TimeUnit timeUnit, Runnable action)
```
Try to get a lock of the key in non-block way. 
Return a instance of ```AutoReleaseLock``` if there is no lock on the specified key, or else null. 
If the ```Cache``` is an in-memory implementation, the lock is a local lock in the JVM,
or else it's a non-strict distributed lock.
The expire time of the lock specified by ```expire``` and ```timeUnit```.
```MultiLevelCache``` use the last level to acquire the lock.
Here is an example:
```java
  // use try-with-resource to auto release the lock
  try(AutoReleaseLock lock = cache.tryLock("MyKey",100, TimeUnit.SECONDS)){
     if(lock != null){
        // do something
     }
  }
```
Here is a simpler way that you never forget ```if(lock != null)```:
```java
  boolean hasRun = cache.tryLockAndRun("MyKey",100, TimeUnit.SECONDS, () -> {
    // do something
  });
```
When the cache system is distributed, the ```tryLock``` will carefully auto retry when network fails, and it never release a lock that the current machine doesn't hold. Using ```tryLock``` or ```tryLockAndRun``` are much easier than make your own lock based on a cache system.
Be keep in mind that the distributed lock based on a cache system is non-strict, 
if you need strict distributed lock you should consider other framework like Zoo Keeper.

# Upper case API
Operation like ```V get(K key)``` are convenient but it can not tell more information when it returns null. 
So JetCache provide some more operation which return a ```CacheResult``` object like belows:
```java
CacheGetResult<V> GET(K key);
MultiGetResult<K, V> GET_ALL(Set<? extends K> keys);
CacheResult PUT(K key, V value);
CacheResult PUT(K key, V value, long expireAfterWrite, TimeUnit timeUnit);
CacheResult PUT_ALL(Map<? extends K, ? extends V> map);
CacheResult PUT_ALL(Map<? extends K, ? extends V> map, long expireAfterWrite, TimeUnit timeUnit);
CacheResult REMOVE(K key);
CacheResult REMOVE_ALL(Set<? extends K> keys);
CacheResult PUT_IF_ABSENT(K key, V value, long expireAfterWrite, TimeUnit timeUnit);
```

The name of these method are all uppercase. The usage are more complex and powerful:
```java
CacheGetResult<OrderDO> r = cache.GET(orderId);
if( r.isSuccess() ){
    OrderDO order = r.getValue();
} else if (r.getResultCode() == CacheResultCode.NOT_EXISTS) {
    System.out.println("cache miss:" + orderId);
} else if(r.getResultCode() == CacheResultCode.EXPIRED) {
    System.out.println("cache expired:" + orderId));
} else {
    System.out.println("cache get error:" + orderId);
}
```