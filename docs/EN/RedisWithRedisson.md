> [!NOTE]  
> There are various client libraries for redis:
> JetCache support *jedis*, *lettuce* and *redisson*.
> In JetCache, the lettuce client brings asynchronous access and cluster support.

Add *jetcache-redssion* or *jetcache-starter-redssion*(spring boot) to *pom.xml* if you choose to using redisson client.
# with spring boot

JetCache Redisson does not support creating clients through configuration, but instead obtains the Client instance by getting the Spring bean. 
Therefore, when using Redisson support in Spring Boot, provide redisson client spring bean name through `redissonClient` property is enough.

application.yml (without local cache configurations):

```
jetcache:
  areaInCacheName: false
  remote:
    default:
      type: redisson
      redissonClient: redisClientBeanName
      broadcastChannel: projectA
      keyConvertor: fastjson2
      defaultExpireInMillis: 10000
      keyPrefix: spring-data-redis
```

# without Spring Boot
```java
@Configuration
@EnableMethodCache(basePackages = "com.company.mypackage")
@EnableCreateCacheAnnotation
@Import(JetCacheBaseBeans.class) //need since jetcache 2.7+
public class JetCacheConfig {

    @Bean
    public RedissonClient redissonClient(){
        // build RedissonClient ...
    }

    //@Bean for jetcache <=2.6 
    //public SpringConfigProvider springConfigProvider() {
    //    return new SpringConfigProvider();
    //}

    @Bean
    public GlobalCacheConfig config(RedissonClient redissonClient){
        Map localBuilders = new HashMap();
        EmbeddedCacheBuilder localBuilder = LinkedHashMapCacheBuilder
                .createLinkedHashMapCacheBuilder()
                .keyConvertor(Fastjson2KeyConvertor.INSTANCE);
        localBuilders.put(CacheConsts.DEFAULT_AREA, localBuilder);

        Map remoteBuilders = new HashMap();
        RedissonCacheBuilder remoteCacheBuilder = RedissonCacheBuilder.createBuilder()
                .keyConvertor(Fastjson2KeyConvertor.INSTANCE)
                .valueEncoder(JavaValueEncoder.INSTANCE)
                .valueDecoder(JavaValueDecoder.INSTANCE)
                .broadcastChannel("projectA")
                .redissonClient(redissonClient);
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
Config config = new Config();
config.useSingleServer().setAddress("redis://127.0.0.1:6379").setDatabase(0);

Cache<String, String> cache = RedissonCacheBuilder.createBuilder()
        .redissonClient(Redisson.create(config))
        .keyConvertor(FastjsonKeyConvertor.INSTANCE)
        .valueEncoder(JavaValueEncoder.INSTANCE)
        .valueDecoder(JavaValueDecoder.INSTANCE)
        .expireAfterWrite(200, TimeUnit.SECONDS)
        .buildCache();

```



