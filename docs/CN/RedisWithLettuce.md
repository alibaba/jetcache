> [!NOTE]  
> Redis 有多种 Java 版本的客户端:
> - JetCache 2.2 以前使用 Jedis 客户端访问 Redis。
> - 从 JetCache 2.2 版本开始，增加了对 Lettuce 客户端的支持，JetCache 的 Lettuce 支持提供了异步操作和 Redis 集群支持。
> - 从 JetCache 2.7 版本开始，增加了对 Redisson 客户端的支持。

如果选用 Lettuce 访问 Redis，对应的 Maven Artifact 是 `jetcache-redis-lettuce` 和 `jetcache-starter-redis-lettuce`。lettuce 使用 Netty 建立并复用单个连接实现 redis 的通信，因此无须配置连接池。

# spring boot环境下的lettuce支持
application.yml文件如下（这里省去了local相关的配置）：
```
jetcache: 
  areaInCacheName: false
  remote:
    default:
      type: redis.lettuce
      keyConvertor: fastjson2
      broadcastChannel: projectA
      uri: redis://127.0.0.1:6379/
      #uri: redis-sentinel://127.0.0.1:26379,127.0.0.1:26380,127.0.0.1:26381/?sentinelMasterId=mymaster
      #readFrom: slavePreferred
```
如果使用sentinel做自动主备切换，uri可以配置为redis-sentinel://127.0.0.1:26379,127.0.0.1:26380,127.0.0.1:26381/?sentinelMasterId=mymaster

readFrom的取值可以看[ReadFrom](https://github.com/lettuce-io/lettuce-core/blob/master/src/main/java/io/lettuce/core/ReadFrom.java)类的valueOf方法。

如果是集群：
```
jetcache: 
  areaInCacheName: false
  remote:
    default:
      type: redis.lettuce
      keyConvertor: fastjson2
      broadcastChannel: projectA
      mode: cluster
      #readFrom: slavePreferred
      uri:
        - redis://127.0.0.1:7000
        - redis://127.0.0.1:7001
        - redis://127.0.0.1:7002
```


# 不使用spring boot
```java
@Configuration
@EnableMethodCache(basePackages = "com.company.mypackage")
@EnableCreateCacheAnnotation
@Import(JetCacheBaseBeans.class) //need since jetcache 2.7+
public class JetCacheConfig {

    @Bean
    public RedisClient redisClient(){
        RedisClient client = RedisClient.create("redis://127.0.0.1");
        client.setOptions(ClientOptions.builder().
               disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
               .build());
        return client;
    }

    //@Bean for jetcache <=2.6 
    //public SpringConfigProvider springConfigProvider() {
    //    return new SpringConfigProvider();
    //}

    @Bean
    public GlobalCacheConfig config(RedisClient redisClient){
        Map localBuilders = new HashMap();
        EmbeddedCacheBuilder localBuilder = LinkedHashMapCacheBuilder
                .createLinkedHashMapCacheBuilder()
                .keyConvertor(Fastjson2KeyConvertor.INSTANCE);
        localBuilders.put(CacheConsts.DEFAULT_AREA, localBuilder);

        Map remoteBuilders = new HashMap();
        RedisLettuceCacheBuilder remoteCacheBuilder = RedisLettuceCacheBuilder.createRedisLettuceCacheBuilder()
                .keyConvertor(Fastjson2KeyConvertor.INSTANCE)
                .valueEncoder(JavaValueEncoder.INSTANCE)
                .valueDecoder(JavaValueDecoder.INSTANCE)
                .broadcastChannel("projectA")
                .redisClient(redisClient);
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
如果不通过@CreateCache和@Cached注解，可以通过下面的方式创建Cache。通过注解创建的缓存会自动设置keyPrefix，这里是手工创建缓存，对于远程缓存需要设置keyPrefix属性，以免不同Cache实例的key发生冲突。
```java
RedisClient client = RedisClient.create("redis://127.0.0.1");

Cache<Long,OrderDO> orderCache = RedisLettuceCacheBuilder.createRedisLettuceCacheBuilder()
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .valueEncoder(JavaValueEncoder.INSTANCE)
                .valueDecoder(JavaValueDecoder.INSTANCE)
                .redisClient(client)
                .keyPrefix("orderCache")
                .expireAfterWrite(200, TimeUnit.SECONDS)
                .buildCache();
```
