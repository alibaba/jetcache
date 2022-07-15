
# 简介
JetCache2.0的核心是```com.alicp.jetcache.Cache```接口（以下简写为```Cache```），它提供了部分类似于```javax.cache.Cache```（JSR107）的API操作。没有完整实现JSR107的原因包括：
1. 希望维持API的简单易用。
1. 对于特定的远程缓存系统来说，```javax.cache.Cache```中定义的有些操作无法高效率的实现，比如一些原子操作方法和类似```removeAll()```这样的方法。
1. JSR107比较复杂，完整实现要做的工作很多。

# JSR107 style API
以下是Cache接口中和JSR107的javax.cache.Cache接口一致的方法，除了不会抛出异常，这些方法的签名和行为和JSR107都是一样的。
```java
V get(K key)
void put(K key, V value);
boolean putIfAbsent(K key, V value); //多级缓存MultiLevelCache不支持此方法
boolean remove(K key);
<T> T unwrap(Class<T> clazz);//2.2版本前，多级缓存MultiLevelCache不支持此方法
Map<K,V> getAll(Set<? extends K> keys);
void putAll(Map<? extends K,? extends V> map);
void removeAll(Set<? extends K> keys);
```

# JetCache特有API
```java
V computeIfAbsent(K key, Function<K, V> loader)
```
当key对应的缓存不存在时，使用loader加载。通过这种方式，loader的加载时间可以被统计到。

```java
V computeIfAbsent(K key, Function<K, V> loader, boolean cacheNullWhenLoaderReturnNull)
```
当key对应的缓存不存在时，使用loader加载。cacheNullWhenLoaderReturnNull参数指定了当loader加载出来时null值的时候，是否要进行缓存（有时候即使是null值也是通过很繁重的查询才得到的，需要缓存）。

```java
V computeIfAbsent(K key, Function<K, V> loader, boolean cacheNullWhenLoaderReturnNull, long expire, TimeUnit timeUnit)
```
当key对应的缓存不存在时，使用loader加载。cacheNullWhenLoaderReturnNull参数指定了当loader加载出来时null值的时候，是否要进行缓存（有时候即使是null值也是通过很繁重的查询才得到的，需要缓存）。expire和timeUnit指定了缓存的超时时间，会覆盖缓存的默认超时时间。

```java
void put(K key, V value, long expire, TimeUnit timeUnit)
```
put操作，expire和timeUnit指定了缓存的超时时间，会覆盖缓存的默认超时时间。

```java
AutoReleaseLock tryLock(K key, long expire, TimeUnit timeUnit)
boolean tryLockAndRun(K key, long expire, TimeUnit timeUnit, Runnable action)
```
非堵塞的尝试获取一个锁，如果对应的key还没有锁，返回一个AutoReleaseLock，否则立即返回空。如果Cache实例是本地的，它是一个本地锁，在本JVM中有效；如果是redis等远程缓存，它是一个不十分严格的分布式锁。锁的超时时间由expire和timeUnit指定。多级缓存的情况会使用最后一级做tryLock操作。用法如下：
```java
  // 使用try-with-resource方式，可以自动释放锁
  try(AutoReleaseLock lock = cache.tryLock("MyKey",100, TimeUnit.SECONDS)){
     if(lock != null){
        // do something
     }
  }
```
上面的代码有个潜在的坑是忘记判断if(lock!=null)，所以一般可以直接用tryLockAndRun更加简单
```java
  boolean hasRun = cache.tryLockAndRun("MyKey",100, TimeUnit.SECONDS, () -> {
    // do something
  });
```
tryLock内部会在访问远程缓存失败时重试，会自动释放，而且不会释放不属于自己的锁，比你自己做这些要简单。当然，基于远程缓存实现的任何分布式锁都不会是严格的分布式锁，不能和基于ZooKeeper或Consul做的锁相比。

# 大写API
V get(K key)这样的方法虽然用起来方便，但有功能上的缺陷，当get返回null的时候，无法断定是对应的key不存在，还是访问缓存发生了异常，所以JetCache针对部分操作提供了另外一套API，提供了完整的返回值，包括：
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
这些方法的特征是方法名为大写，与小写的普通方法对应，提供了完整的返回值，用起来也稍微繁琐一些。例如：
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