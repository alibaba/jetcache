# spring兼容性
jetcache在以下spring/spring-boot版本下通过了测试，如果你只用部分功能或者能自己调整依赖的的话，适用范围还可以更大一些。

| jetcache版本 | spring版本                    | spring boot版本               | 说明                                                                            |
|------------|-----------------------------|-----------------------------|-------------------------------------------------------------------------------|
| 2.5        | 4.0.8.RELEASE~5.1.1.RELEASE | 1.1.9.RELEASE~2.0.5.RELEASE ||
| 2.6        | 5.0.4.RELEASE~5.2.4.RELEASE | 2.0.0.RELEASE~2.2.5.RELEASE | jetcache-redis依赖jedis3.1.0，spring-data(jedis，boot版本<=2.1.X)依赖jedis2.9.3，不能同时用 |
| 2.7        | 5.2.4.RELEASE~5.3.23        | 2.2.5.RELEASE~2.7.5         | jetcahe-redis依赖jedis4，spring-data(jedis)依赖jedis3，不能同时用                        |
| 2.7.4      | 5.2.4.RELEASE~6.0.11        | 2.2.5.RELEASE~3.1.3         | |

# 兼容性改动说明
## 2.7.4
* 默认传递依赖spring-boot 3.1.3，spring-framework 6.0.11，slf4j-api 2.x
* 移除了javax.annotation:javax.annotation-api这个依赖，如果你用了@PostConstruct等注解，可能需要自己加上这个依赖

## 2.7.2
* 更新了redisson的编码方式，和2.7.1不兼容

## 2.7.0
* jetcahe-redis依赖jedis4，如果你使用spring data并且使用jedis的话（spring-data默认用lettuce），它需要3，所以你需要自己把版本改回去，并且不能再使用jetcahe-redis了（改用jetcache-redis-springdata）
* encoder/decoder现在同时支持kryo4和kryo5，在yml中"kryo"仍然代表kryo4，"kryo5"代表kryo5。kryo4和kryo5的序列化内容完全不兼容。
  * kryo4对应的依赖是com.esotericsoftware:kryo，kryo5对应的依赖是com.esotericsoftware.kryo:kryo5
  * kryo4和kryo5可以并存，maven id和包名都不一样。
  * 要注意com.esotericsoftware:kryo的版本号也可以改为5.x.x
* lettuce连接redis cluster需要在yml里面指定mode=cluster
* 默认的key convertor改成了"fastjson2"，fastjson2和fastjson可以并存，fastjson（非fastjson2）/kryo/kryo5/mvel在maven中都改为optional，如果使用了需要用户手工声明依赖
* 如果没有使用spring boot，应该增加```@Import(JetCacheBaseBeans.class)```，同时删除原来定义的configProvider bean，具体例子可以看最新文档
* GlobalCacheConfig.areaInCacheName默认值改为false，以前所有的代码案例都显式的写了areaInCacheName=false，不会有人没加这一行吧

## 2.6.0
* GET/GET_ALL方法不再触发自动刷新（大写的方法只简单访问缓存， 小写的方法才能触发这些附加功能）
* 不再支持lettuce4
* 不再支持jedis2.9

## 2.5.0
* 从2.3.3及更低版本升级到2.5.0会发生ClassCastException（如果你使用了MultiLevelCache或者cacheType.CacheType.BOTH）。
解决办法是先升级到2.4.4并且发布到生产环境，然后再升级到2.5.0。
* 子类的注解会覆盖接口和父类