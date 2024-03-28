> [!NOTE]  
> Redis 有多种 Java 版本的客户端:
> - JetCache 2.2 以前使用 Jedis 客户端访问 Redis。
> - 从 JetCache 2.2 版本开始，增加了对 Lettuce 客户端的支持，JetCache 的 Lettuce 支持提供了异步操作和 Redis 集群支持。
> - 从 JetCache 2.7 版本开始，增加了对 Redisson 客户端的支持。

如果选用 Redisson 访问 Redis，对应的 Maven Artifact 是 `jetcache-redisson` 和 `jetcache-starter-redisson`。

# spring boot环境下的redisson支持

redisson 不支持通过配置连接，而是通过获取 Spring bean 拿到 Client 实例，因此在 Spring Boot 环境下使用 redisson 支持时，客户端特定的参数只需要配置一个
`redissonClient` 即可。

application.yml文件如下（这里省去了local相关的配置）：
```
jetcache:
  areaInCacheName: false
  remote:
    default:
      type: redisson
      redissonClient: redisClientBeanName
      # 下面是通用配置, 只列举了一些
      broadcastChannel: projectA
      keyConvertor: fastjson2
      defaultExpireInMillis: 10000
      keyPrefix: spring-data-redis
```

# 不使用spring boot
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

# Builder API

如果不通过 `@CreateCache` 和` @Cached` 注解，可以通过下面的方式创建 Cache。通过注解创建的缓存会自动设置 keyPrefix，这里是手工创建缓存，对于远程缓存需要设置 keyPrefix 属性，以免不同Cache实例的key发生冲突。

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



