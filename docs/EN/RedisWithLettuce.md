
There are various client libraries for redis. JetCache support *jedis* and *lettuce*. 
In JetCache, the lettuce client brings asynchronous access and cluster support. 

Add *jetcache-redis-lettuce* or *jetcache-starter-redis-lettuce*(spring boot) to *pom.xml* if you choose to using lettuce client.
> Note: The groupId and java package name changed in lettuce5. *jetcache-redis-lettuce* and *jetcache-starter-redis-lettuce* support lettuce5, *jetcache-redis-lettuce4* and *jetcache-starter-redis-lettuce4* support lettuce4. 


# with Spring Boot
application.yml (without local cache configurations):
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

If redis server is configures using sentinel for HA, the uri can be ```redis-sentinel://127.0.0.1:26379,127.0.0.1:26380,127.0.0.1:26381/?sentinelMasterId=mymaster```.

The definition of readFrom can be found in ```valueOf``` method of [ReadFrom](https://github.com/lettuce-io/lettuce-core/blob/master/src/main/java/io/lettuce/core/ReadFrom.java) class.

If redis server is a cluster:
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


# without Spring Boot
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

# builder API
You can use builder API to build ```RedisLettuceCache``` if you don't want to use any annotation.
The ```keyPrefix``` will add to key as prefix.
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
