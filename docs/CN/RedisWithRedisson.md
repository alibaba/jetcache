
> > Redis 有多种 Java 版本的客户端，JetCache 2.2 以前使用 Jedis 客户端访问 Redis。从 JetCache 2.2 版本开始，增加了对 Lettuce 客户端的支持，JetCache 的 Lettuce 支持提供了异步操作和 Redis 集群支持。

如果选用 Redisson 访问 Redis，对应的 Maven Artifact 是 `jetcache-redisson` 和 `jetcache-starter-redisson`。

# spring boot环境下的redisson支持

application.yml文件如下（这里省去了local相关的配置）：
```
jetcache: 
  areaInCacheName: false
  remote:
    default:
      type: redis
      keyConvertor: fastjson2
      broadcastChannel: projectA
      poolConfig:
        minIdle: 5
        maxIdle: 20
        maxTotal: 50
      host: ${redis.host}
      port: ${redis.port}
      #password:***
      #sentinels: 127.0.0.1:26379 , 127.0.0.1:26380, 127.0.0.1:26381
      #masterName: mymaster
```

如果需要直接操作JedisPool，可以通过以下方式获取
```java
@Bean(name = "defaultPool")
@DependsOn(RedisAutoConfiguration.AUTO_INIT_BEAN_NAME)//jetcache2.2+
//@DependsOn("redisAutoInit")//jetcache2.1
public JedisPoolFactory defaultPool() {
    return new JedisPoolFactory("remote.default", JedisPool.class);
}
```
然后可以直接使用
```java
@Autowired
private Pool<Jedis> defaultPool;
```

也可以用```Cache```接口上的```<T> T unwrap(Class<T> clazz)```方法来获取JedisPool，参见RedisCache.unwrap源代码。

# 不使用spring boot
```java
@Configuration
@EnableMethodCache(basePackages = "com.company.mypackage")
@EnableCreateCacheAnnotation
@Import(JetCacheBaseBeans.class) //need since jetcache 2.7+
public class JetCacheConfig {

    @Bean
    public Pool<Jedis> pool(){
        // build jedis pool ...
    }

    //@Bean for jetcache <=2.6 
    //public SpringConfigProvider springConfigProvider() {
    //    return new SpringConfigProvider();
    //}

    @Bean
    public GlobalCacheConfig config(Pool<Jedis> pool){
        Map localBuilders = new HashMap();
        EmbeddedCacheBuilder localBuilder = LinkedHashMapCacheBuilder
                .createLinkedHashMapCacheBuilder()
                .keyConvertor(Fastjson2KeyConvertor.INSTANCE);
        localBuilders.put(CacheConsts.DEFAULT_AREA, localBuilder);

        Map remoteBuilders = new HashMap();
        RedisCacheBuilder remoteCacheBuilder = RedisCacheBuilder.createRedisCacheBuilder()
                .keyConvertor(FastjsonKey2Convertor.INSTANCE)
                .valueEncoder(JavaValueEncoder.INSTANCE)
                .valueDecoder(JavaValueDecoder.INSTANCE)
                .broadcastChannel("projectA")
                .jedisPool(pool);
        remoteBuilders.put(CacheConsts.DEFAULT_AREA, remoteCacheBuilder);

        GlobalCacheConfig globalCacheConfig = new GlobalCacheConfig();
        globalCacheConfig.setLocalCacheBuilders(localBuilders);
        globalCacheConfig.setRemoteCacheBuilders(remoteBuilders);
        globalCacheConfig.setStatIntervalMinutes(15);
        globalCacheConfig.setAreaInCacheName(false);

        return globalCacheConfig;
    }
}
```

# Builder API
如果不通过@CreateCache和@Cached注解，可以通过下面的方式创建RedisCache。通过注解创建的缓存会自动设置keyPrefix，这里是手工创建缓存，对于远程缓存需要设置keyPrefix属性，以免不同Cache实例的key发生冲突。
```java
GenericObjectPoolConfig pc = new GenericObjectPoolConfig();
pc.setMinIdle(2);
pc.setMaxIdle(10);
pc.setMaxTotal(10);
JedisPool pool = new JedisPool(pc, "localhost", 6379);

Cache<Long,OrderDO> orderCache = RedisCacheBuilder.createRedisCacheBuilder()
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .valueEncoder(JavaValueEncoder.INSTANCE)
                .valueDecoder(JavaValueDecoder.INSTANCE)
                .jedisPool(pool)
                .keyPrefix("orderCache")
                .expireAfterWrite(200, TimeUnit.SECONDS)
                .buildCache();
```


# Spring Boot环境下的 Jedis 的集群模式支持

`application.yml` 文件如下（这里省去了local相关的配置）：

> Cluster 模式下，大部分配置和单机模式通用，只是需要配置 `cluster` 和可选的 `maxAttempt` 属性，指定集群的多个节点，而单机模式下只需要填写 `host` 和 `port` 即可。

```yml
jetcache: 
  areaInCacheName: false
  remote:
    default:
      type: redis
      keyConvertor: fastjson2
      broadcastChannel: projectA
      poolConfig:
        minIdle: 5
        maxIdle: 20
        maxTotal: 50
      # 通用配置
      timeout: 2000
      connectionTimeout: 2000
      soTimeout: 2000
      # 按需选配的通用配置
      #user: ***  
      #password:***
      #clientName: 
      #ssl: false
      # 集群特定配置
      cluster:
        - 127.0.0.1:6379
        - 127.0.0.1:6378
        - 127.0.0.1:6377
      maxAttempt: 5
```

如果需要直接操作 JedisCluster，可以通过以下方式获取：

```java
@Bean(name = "defaultCluster")
@DependsOn(RedisAutoConfiguration.AUTO_INIT_BEAN_NAME)//jetcache2.2+
//@DependsOn("redisAutoInit")//jetcache2.1
public JedisFactory defaultCluster() {
    return new JedisFactory("remote.default", JedisCluster.class);
}
```
然后直接在 Spring Bean 中使用：

```java
@Autowired
private JedisCluster defaultCluster;
```

# Spring Boot环境下的 Jedis 的哨兵模式支持

`application.yml` 文件如下（这里省去了local相关的配置）：

> 哨兵模式下，大部分配置和单机模式通用，但需要配置额外的哨兵配置

```yml
jetcache: 
  areaInCacheName: false
  remote:
    default:
      type: redis
      keyConvertor: fastjson2
      broadcastChannel: projectA
      poolConfig:
        minIdle: 5
        maxIdle: 20
        maxTotal: 50
      # 通用配置
      timeout: 2000
      connectionTimeout: 2000
      soTimeout: 2000
      # 按需选配的通用配置
      #user: ***  
      #password:***
      #clientName: 
      #ssl: false
      # 哨兵特定配置
      sentinels: 127.0.0.1:26379 , 127.0.0.1:26380, 127.0.0.1:26381
      masterName: mymaster
      sentinelConnectionTimeout: 2000
      sentinelSoTimeout: 2000
      # 哨兵可选配置
      #sentinelUser: ***
      #sentinelPassword: ***
      #sentinelClientName: 
    
```

如果需要直接操作 JedisSentinelPool，可以通过以下方式获取：

```java
@Bean(name = "defaultSentinelPool")
@DependsOn(RedisAutoConfiguration.AUTO_INIT_BEAN_NAME)//jetcache2.2+
//@DependsOn("redisAutoInit")//jetcache2.1
public JedisSentinelPool defaultSentinelPool() {
    return new JedisPoolFactory("remote.default", JedisSentinelPool.class);
}
```
然后直接在 Spring Bean 中使用：

```java
@Autowired
private JedisSentinelPool defaultSentinelPool;
```


