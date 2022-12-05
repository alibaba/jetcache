
![JetCache logo](../images/logo_jetcache.png)

## 简介
JetCache是一个基于Java的缓存系统封装，提供统一的API和注解来简化缓存的使用。
JetCache提供了比SpringCache更加强大的注解，可以原生的支持TTL、两级缓存、分布式自动刷新，还提供了```Cache```接口用于手工缓存操作。
当前有四个实现，```RedisCache```、```TairCache```（此部分未在github开源）、```CaffeineCache```(in memory)和一个简易的```LinkedHashMapCache```(in memory)，要添加新的实现也是非常简单的。

全部特性:
* 通过统一的API访问Cache系统
* 通过注解实现声明式的方法缓存，支持TTL和两级缓存
* 通过注解创建并配置```Cache```实例
* 针对所有```Cache```实例和方法缓存的自动统计
* Key的生成策略和Value的序列化策略是可以配置的
* 分布式缓存自动刷新，分布式锁 (2.2+)
* 异步Cache API (2.2+，使用Redis的lettuce客户端时)
* Spring Boot支持

## 要求
JetCache需要JDK1.8、Spring Framework4.0.8以上版本。Spring Boot为可选，需要1.1.9以上版本。如果不使用注解（仅使用jetcache-core），Spring Framework也是可选的，此时使用方式与Guava/Caffeine cache类似。

## 例子
参考工程samples目录

## 文档目录
* [快速入门](GettingStarted.md)
* [基本Cache API](CacheAPI.md)
* [通过CacheManager注解创建```Cache```实例](CreateCache.md)
* [通过注解实现方法缓存](MethodCache.md)
* [配置详解](Config.md)
* [高级Cache API](AdvancedCacheAPI.md)
* Redis支持(redis客户端任选一即可)
  * [使用jedis客户端连接redis](RedisWithJedis.md)
  * [使用lettuce客户端连接redis](RedisWithLettuce.md)
  * Spring data redis (文档待写)
* [内存缓存```LinkedHashMapCache```和```CaffeineCache```](Embedded.md)
* [统计](Stat.md)
* [Builder](Builder.md)：未使用Spring4（或未使用Spring）的时候，或通过Builder手工构造```Cache```
* [开发者文档](DevNote.md)
* [升级和兼容性指南](Compatibility.md)
* [FAQ](FAQ.md)

## 依赖哪个jar？
* jetcache-anno-api：定义jetcache的注解和常量，不传递依赖。如果你想把Cached注解加到接口上，又不希望你的接口jar传递太多依赖，可以让接口jar依赖jetcache-anno-api。
* jetcache-core：核心api，完全通过编程来配置操作```Cache```，不依赖Spring。两个内存中的缓存实现```LinkedHashMapCache```和```CaffeineCache```也由它提供。
* jetcache-anno：基于Spring提供@Cached和@CreateCache注解支持。
* jetcache-redis：使用jedis提供Redis支持。
* jetcache-redis-lettuce（需要JetCache2.3以上版本）：使用lettuce提供Redis支持，实现了JetCache异步访问缓存的的接口。
* jetcache-starter-redis：Spring Boot方式的Starter，基于Jedis。
* jetcache-starter-redis-lettuce（需要JetCache2.3以上版本）：Spring Boot方式的Starter，基于Lettuce。

