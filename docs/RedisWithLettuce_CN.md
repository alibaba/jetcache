[English](RedisWithLettuce)

redis有多种java版本的客户端，JetCache2.2以前使用jedis客户端访问redis。从JetCache2.2版本开始，增加了对lettuce客户端的支持，JetCache的lettuce支持提供了异步操作和redis集群支持。

使用lettuce访问redis，对应的maven artifact是jetcache-redis-lettuce和jetcache-starter-redis-lettuce。lettuce使用Netty建立单个连接连redis，所以不需要配置连接池。

> 注意：新发布的lettuce5更换了groupId和包名，2.3版本的JetCache同时支持lettuce4和5，jetcache-redis-lettuce，jetcache-starter-redis-lettuce提供lettuce5支持，jetcache-redis-lettuce4和jetcache-starter-redis-lettuce4提供lettuce4支持。

> 注意：JetCache2.2版本中，lettuce单词存在错误的拼写，错写为“luttece”，该错误存在于包名、类名和配置中，2.3已经改正。

# spring boot环境下的lettuce支持
application.yml文件如下（这里省去了local相关的配置）：
```
jetcache: 
  areaInCacheName: false
  remote:
    default:
      type: redis.lettuce
      keyConvertor: fastjson
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
      keyConvertor: fastjson
      #readFrom: slavePreferred
      uri:
        - redis://127.0.0.1:7000
        - redis://127.0.0.1:7001
        - redis://127.0.0.1:7002
```

如果需要直接使用lettuce的RedisClient：
```java
@Bean(name = "defaultClient")
@DependsOn(RedisLettuceAutoConfiguration.AUTO_INIT_BEAN_NAME)
public LettuceFactory defaultClient() {
    return new LettuceFactory("remote.default", RedisClient.class);
}
```
然后可以直接使用
```java
@Autowired
private RedisClient defaultClient;
```

也可以用```Cache```接口上的```<T> T unwrap(Class<T> clazz)```方法来获取```RedisClient```和```RedisCommands```等。
参考RedisLettuceCache.unwrap源代码。

# 不使用spring boot
```java
@Configuration
@EnableMethodCache(basePackages = "com.company.mypackage")
@EnableCreateCacheAnnotation
public class JetCacheConfig {

    @Bean
    public RedisClient redisClient(){
        RedisClient client = RedisClient.create("redis://127.0.0.1");
        client.setOptions(ClientOptions.builder().
               disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
               .build());
        return client;
    }

    @Bean
    public SpringConfigProvider springConfigProvider() {
        return new SpringConfigProvider();
    }

    @Bean
    public GlobalCacheConfig config(SpringConfigProvider configProvider,RedisClient redisClient){
        Map localBuilders = new HashMap();
        EmbeddedCacheBuilder localBuilder = LinkedHashMapCacheBuilder
                .createLinkedHashMapCacheBuilder()
                .keyConvertor(FastjsonKeyConvertor.INSTANCE);
        localBuilders.put(CacheConsts.DEFAULT_AREA, localBuilder);

        Map remoteBuilders = new HashMap();
        RedisLettuceCacheBuilder remoteCacheBuilder = RedisLettuceCacheBuilder.createRedisLettuceCacheBuilder()
                .keyConvertor(FastjsonKeyConvertor.INSTANCE)
                .valueEncoder(JavaValueEncoder.INSTANCE)
                .valueDecoder(JavaValueDecoder.INSTANCE)
                .redisClient(redisClient);
        remoteBuilders.put(CacheConsts.DEFAULT_AREA, remoteCacheBuilder);

        GlobalCacheConfig globalCacheConfig = new GlobalCacheConfig();
        //globalCacheConfig.setConfigProvider(configProvider);//for jetcache <=2.5
        globalCacheConfig.setLocalCacheBuilders(localBuilders);
        globalCacheConfig.setRemoteCacheBuilders(remoteBuilders);
        globalCacheConfig.setStatIntervalMinutes(15);
        globalCacheConfig.setAreaInCacheName(false);

        return globalCacheConfig;
    }
}
```

# builder API
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
