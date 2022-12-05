
redis有多种java版本的客户端，JetCache2.2以前使用jedis客户端访问redis。从JetCache2.2版本开始，增加了对luttece客户端的支持，jetcache的luttece支持提供了异步操作和redis集群支持。

如果选用jedis访问redis，对应的maven artifact是jetcache-redis和jetcache-starter-redis(spring boot)。
# spring boot环境下的jedis支持
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

