# spring兼容性
jetcache在以下spring/spring-boot版本下通过了测试，如果你只用部分功能或者能自己调整依赖的的话，适用范围还可以更大一些。

| jetcache版本 | spring版本                    | spring boot版本               | 说明                                                                            |
|------------|-----------------------------|-----------------------------|-------------------------------------------------------------------------------|
| 2.5        | 4.0.8.RELEASE~5.1.1.RELEASE | 1.1.9.RELEASE~2.0.5.RELEASE ||
| 2.6        | 5.0.4.RELEASE~5.2.4.RELEASE | 2.0.0.RELEASE~2.2.5.RELEASE | jetcache-redis依赖jedis3.1.0，spring-data(jedis，boot版本<=2.1.X)依赖jedis2.9.3，不能同时用 |
| 2.7        | 5.2.4.RELEASE~5.3.23        | 2.2.5.RELEASE~2.7.5         | jetcahe-redis依赖jedis4，spring-data(jedis)依赖jedis3，不能同时用                        |
| 2.7.4+     | 5.2.4.RELEASE~6.2.18        | 2.2.5.RELEASE~3.5.14        | 其实也可以支持Spring 7/Spring Boot 4，只是pom中定义的是Spring 6/Spring Boot 3 |
| 2.8        | 6.x~7.0.7                   | 3.x~4.0.6                   | 需要Java 17+；BOM默认依赖Spring Framework 7.0.7 / Spring Boot 4.0.6 / Spring Data Redis 4.0.5 / SLF4J 2.x |

# 兼容性改动说明
## 2.8.0
* Java 17为最低要求版本
* `areaInCacheName`默认值已改为`false`（2.8.0之前为`true`）。
* 不再支持kryo4，`com.esotericsoftware:kryo`已升级到5.x，`SerialPolicy`中的`KRYO`常量现在也使用kryo5实现。kryo4序列化数据与kryo5不兼容，升级前需等待旧缓存过期或手动清空
* 移除了fastjson1支持，`fastjson` key convertor现在内部使用fastjson2实现。如果你需要fastjson1，需自行添加依赖并实现自定义KeyConvertor
* 移除了Spring XML namespace支持（XML配置中的`<jetcache:xxx>`标签不再可用）
* 新增反序列化过滤器机制（默认开启）。这是一个**破坏性变更**，如果缓存值中包含不在默认允许列表中的自定义类，升级后反序列化（或序列化）会直接失败。
  
  **升级步骤**：由于旧版本没有此配置项，无法在升级前预配置。推荐以下两种方式：
  
  方式一：升级JetCache的**同时**添加`decodeFilterAllowPatterns`配置，将自定义类所在的包名加入白名单。例如：
  ```yaml
  jetcache:
    decodeFilterAllowPatterns:
      - com.yourcompany.
  ```
  
  方式二：升级时先关闭过滤器（行为与2.7一致）：
  ```yaml
  jetcache:
    decodeFilterEnabled: false
  ```
  
  关于默认允许列表包含哪些包、以及详细的配置说明，请参见[配置文档](Config.md)中的"反序列化过滤器配置"章节。

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
* GlobalCacheConfig.areaInCacheName默认值改为false(但是有bug，默认值可能还是true)，areaInCacheName=false这个还得加上

## 2.6.0
* GET/GET_ALL方法不再触发自动刷新（大写的方法只简单访问缓存， 小写的方法才能触发这些附加功能）
* 不再支持lettuce4
* 不再支持jedis2.9

## 2.5.0
* 从2.3.3及更低版本升级到2.5.0会发生ClassCastException（如果你使用了MultiLevelCache或者cacheType.CacheType.BOTH）。
解决办法是先升级到2.4.4并且发布到生产环境，然后再升级到2.5.0。
* 子类的注解会覆盖接口和父类
