> [!NOTE]  
> There are various client libraries for redis:
> JetCache support *jedis*, *lettuce* and *redisson*.
> In JetCache, the lettuce client brings asynchronous access and cluster support.

Add *jetcache-redis* or *jetcache-starter-redis*(spring boot) to *pom.xml* if you choose to using jedis client.
# with spring boot
application.yml (without local cache configurations):
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

```JedisPoolFactory``` used to get ```JedisPool``` as a Spring bean: 
```java
@Bean(name = "defaultPool")
@DependsOn(RedisAutoConfiguration.AUTO_INIT_BEAN_NAME)//jetcache2.2+
//@DependsOn("redisAutoInit")//jetcache2.1
public JedisPoolFactory defaultPool() {
    return new JedisPoolFactory("remote.default", JedisPool.class);
}
```

Then you can inject an ```JedisPool``` to you bean using ```@Autowired```: 
```java
@Autowired
private Pool<Jedis> defaultPool;
```

The ```<T> T unwrap(Class<T> clazz)``` method of ```Cache``` can used to get ```JedisPool```.
See code of RedisCache.unwrap for more detail.

# without spring boot
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
                .keyConvertor(Fastjson2KeyConvertor.INSTANCE)
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
You can use builder API to build ```RedisCache``` if you don't want to use any annotation.
The ```keyPrefix``` will add to key as prefix.
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



# Jedis Cluster with spring boot

`application.yml` (without local cache configurations):

> In Cluster mode, most of the configurations are common with Standalone mode.
> The only difference is that you need to configure the `cluster` and optional `maxAttempt` properties to specify multiple nodes in the cluster, 
> while in Standalone mode, you only need to fill in the `host` and `port`.

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
      # common configuration
      timeout: 2000
      connectionTimeout: 2000
      soTimeout: 2000
      # optional configuration
      #user: ***  
      #password:***
      #clientName: 
      #ssl: false
      # cluster specified configuration
      cluster:
        - 127.0.0.1:6379
        - 127.0.0.1:6378
        - 127.0.0.1:6377
      maxAttempt: 5
```

```JedisFactory``` used to get ```JedisCluster``` as a Spring bean: 

```java
@Bean(name = "defaultCluster")
@DependsOn(RedisAutoConfiguration.AUTO_INIT_BEAN_NAME)//jetcache2.2+
//@DependsOn("redisAutoInit")//jetcache2.1
public JedisFactory defaultCluster() {
    return new JedisFactory("remote.default", JedisCluster.class);
}
```
Then you can inject an ```JedisCluster``` to you bean using ```@Autowired```: 

```java
@Autowired
private JedisCluster defaultCluster;
```

# Jedis Sentinels with spring boot

`application.yml` (without local cache configurations):

> In Sentinel mode, most of the configurations are common with Standalone mode.
> The only difference is that you need to configure some specific settings for Sentinel.

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
      # common configuration
      timeout: 2000
      connectionTimeout: 2000
      soTimeout: 2000
      # optional configuration
      #user: ***  
      #password:***
      #clientName: 
      #ssl: false
      # sentinel specified configuration
      sentinels: 127.0.0.1:26379 , 127.0.0.1:26380, 127.0.0.1:26381
      masterName: mymaster
      sentinelConnectionTimeout: 2000
      sentinelSoTimeout: 2000
      # sentinel specified optional configuration
      #sentinelUser: ***
      #sentinelPassword: ***
      #sentinelClientName: 
    
```

```JedisPoolFactory``` used to get ```JedisSentinelPool``` as a Spring bean: 

```java
@Bean(name = "defaultSentinelPool")
@DependsOn(RedisAutoConfiguration.AUTO_INIT_BEAN_NAME)//jetcache2.2+
//@DependsOn("redisAutoInit")//jetcache2.1
public JedisSentinelPool defaultSentinelPool() {
    return new JedisPoolFactory("remote.default", JedisSentinelPool.class);
}
```

Then you can inject an ```JedisSentinelPool``` to you bean using ```@Autowired```: 

```java
@Autowired
private JedisSentinelPool defaultSentinelPool;
```



