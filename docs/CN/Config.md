# 配置说明
yml配置文件案例（如果没使用springboot，直接配置GlobalCacheConfig是类似的，参考快速入门教程）：
```
jetcache:
  statIntervalMinutes: 15
  areaInCacheName: false
  hidePackages: com.alibaba
  local:
    default:
      type: caffeine
      limit: 100
      keyConvertor: fastjson2 #其他可选：fastjson(等同fastjson2)/jackson/jackson3
      expireAfterWriteInMillis: 100000
    otherArea:
      type: linkedhashmap
      limit: 100
      keyConvertor: none
      expireAfterWriteInMillis: 100000
  remote:
    default:
      type: redis
      keyConvertor: fastjson2 #其他可选：fastjson(等同fastjson2)/jackson/jackson3
      broadcastChannel: projectA
      valueEncoder: java #其他可选：kryo/kryo5
      valueDecoder: java #其他可选：kryo/kryo5
      # 同时演示两种解析方式：带 bean: 前缀的 Spring Bean，以及 plain name 对应的 customContainer 条目
      externalWriteInterceptors: bean:loggingExternalCacheWriteInterceptor,auditExternalCacheWriteInterceptor
      poolConfig:
        minIdle: 5
        maxIdle: 20
        maxTotal: 50
      host: ${redis.host}
      port: ${redis.port}
    otherArea:
      type: redis
      keyConvertor: fastjson2 #其他可选：fastjson(等同fastjson2)/jackson/jackson3
      broadcastChannel: projectA
      valueEncoder: java #其他可选：kryo/kryo5
      valueDecoder: java #其他可选：kryo/kryo5
      poolConfig:
        minIdle: 5
        maxIdle: 20
        maxTotal: 50
      host: ${redis.host}
      port: ${redis.port}
```


配置通用说明如下

| 属性 | 默认值                         | 说明                                                                                                                                                                                                    |
| --- |-----------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| jetcache.statIntervalMinutes | 0                           | 统计间隔，0表示不统计                                                                                                                                                                                           |
| jetcache.areaInCacheName | true(2.6-) false(2.7+)      | jetcache-anno把cacheName作为远程缓存key前缀，2.4.3以前的版本总是把areaName加在cacheName中，因此areaName也出现在key前缀中。2.4.4以后可以配置，为了保持远程key兼容默认值为true，但是新项目的话false更合理些，2.7默认值已改为false。                                            |
| jetcache.hiddenPackages | 无                           | @Cached和@CreateCache自动生成name的时候，为了不让name太长，hiddenPackages指定的包名前缀被截掉                                                                                                                                   |
| jetcache.useDefaultLocalExpireInMultiLevelCache | false | 如果设置为true，当cacheType为BOTH且未显式设置localExpire时（包括`@Cached`、`@CreateCache`注解和`QuickConfig` API），本地缓存的过期时间取本地缓存builder上配置的`expireAfterWriteInMillis`与`expire`中的较小值。 |
| jetcache.[local/remote].${area}.type | 无                           | 缓存类型。tair、redis为当前支持的远程缓存；linkedhashmap、caffeine为当前支持的本地缓存类型                                                                                                                                          |
| jetcache.[local/remote].${area}.keyConvertor | fastjson2 | key转换器的全局配置。2.8+支持的keyConvertor：```fastjson2```/```jackson```/```jackson3```（```fastjson```也可用，内部使用fastjson2实现）；仅当使用@CreateCache且缓存类型为LOCAL时可以指定为```none```，此时通过equals方法来识别key。方法缓存必须指定keyConvertor |
| jetcache.[local/remote].${area}.valueEncoder | java                        | 序列化器的全局配置。仅remote类型的缓存需要指定，2.8+可选```java```/```kryo```/```kryo5```（```kryo```和```kryo5```均使用kryo5实现） |
| jetcache.[local/remote].${area}.valueDecoder | java                        | 反序列化器的全局配置。仅remote类型的缓存需要指定，2.8+可选```java```/```kryo```/```kryo5```（```kryo```和```kryo5```均使用kryo5实现） |
| jetcache.[local/remote].${area}.limit | 100                         | 每个缓存实例的最大元素的全局配置，仅local类型的缓存需要指定。注意是每个缓存实例的限制，而不是全部，比如这里指定100，然后用@CreateCache创建了两个缓存实例（并且注解上没有设置localLimit属性），那么每个缓存实例的限制都是100                                                                        |
| jetcache.[local/remote].${area}.expireAfterWriteInMillis | 无穷大                         | 以毫秒为单位指定超时时间的全局配置(以前为defaultExpireInMillis)                                                                                                                                                           |
| jetcache.remote.${area}.broadcastChannel | 无                           | jetcahe2.7的两级缓存支持更新以后失效其他JVM中的local cache，但多个服务共用redis同一个channel可能会造成广播风暴，需要在这里指定channel，你可以决定多个不同的服务是否共用同一个channel。如果没有指定则不开启。                                                                       |
| jetcache.local.${area}.expireAfterAccessInMillis | 0                           | 需要jetcache2.2以上，以毫秒为单位，指定多长时间没有访问，就让缓存失效，当前只有本地缓存支持。0表示不使用这个功能。                                                                                                                                       |
| jetcache.remote.${area}.externalWriteInterceptors | 无                           | 指定外部缓存写入前回调，多个用逗号分隔。`bean:xxx` 表示按 Spring Bean 名称解析；普通名称表示从 `AutoConfigureBeans.customContainer` 中解析。例如：`bean:loggingInterceptor,logging`。回调接收的是只读写入元数据，适合做日志、审计、埋点、大 key 检测等，不用于修改实际编码后的写入请求。返回 `WriteInterceptDecision.reject(...)` 会拒绝当前写操作，并通过缓存结果返回失败；若回调抛出异常，则表示拦截器自身执行异常，同样会通过缓存结果返回，而不会从基础写方法向外传播。像 `@Cached`、`@CreateCache`、`QuickConfig.externalWriteInterceptors(...)` 这样的单 cache 配置会覆盖这个全局配置。 |
| jetcache.decodeFilterEnabled | true | 反序列化过滤器总开关，默认开启。关闭后恢复旧行为（不推荐关闭） |
| jetcache.decodeFilterAllowPatterns | 无 | 用户自定义的允许列表模式，追加到默认允许列表之后。支持三种匹配模式（见下方说明） |
| jetcache.decodeFilterDenyPatterns | 无 | 用户自定义的拒绝列表模式，追加到默认拒绝列表之后。拒绝列表始终优先于允许列表 |

上表中${area}对应@Cached和@CreateCache的area属性。注意如果注解上没有指定area，默认值是"default"。

关于缓存的超时时间，有多个地方指定，澄清说明一下：
1. put等方法上指定了超时时间，则以此时间为准
1. put等方法上未指定超时时间，使用Cache实例的默认超时时间
1. Cache实例的默认超时时间，通过在@CreateCache和@Cached上的expire属性指定，如果没有指定，使用yml中定义的全局配置，例如@Cached(cacheType=local)使用jetcache.local.default.expireAfterWriteInMillis，如果仍未指定则是无穷大

## 反序列化过滤器配置

JetCache 2.8.x 默认开启反序列化过滤器。过滤器同时维护**允许列表**和**拒绝列表**，拒绝列表优先级最高，即使用户添加了允许模式也无法绕过拒绝规则。

默认允许列表如下：

| 模式 | 匹配方式 | 说明 |
| --- | --- | --- |
| `java.lang` | 包名匹配 | String、Integer 等直接类，不含子包（如 reflect、invoke） |
| `java.util.` | 前缀匹配 | 集合类及其子包（如 HashMap、concurrent.ConcurrentHashMap） |
| `java.time.` | 前缀匹配 | 日期时间类（如 LocalDate、Duration） |
| `java.math` | 包名匹配 | BigDecimal、BigInteger 等直接类（该包无子包） |
| `java.net` | 包名匹配 | URI、URL 等直接类，不含子包 |
| `com.alicp.jetcache.` | 前缀匹配 | JetCache 内部类 |

如果缓存值包含自定义类，需要配置允许列表：

```yaml
jetcache:
  decodeFilterEnabled: true  # 默认 true，可设 false 关闭
  decodeFilterAllowPatterns:
    - com.example.          # 前缀匹配：com.example 包及其子包下所有类
    - org.myapp.dto         # 包名匹配：org.myapp.dto 包下的直接类（不含子包）
    - org.myapp.dto.UserDTO # 精确匹配：仅允许这一个类
  decodeFilterDenyPatterns:
    - com.example.internal.      # 拒绝该包及其子包下的类
    - org.myapp.dto.SecretDTO    # 拒绝某一个具体类
```

内置拒绝列表包含已知反序列化攻击 gadget chain（如 Commons Collections、Spring AOP、Hibernate、Groovy、JNDI/RMI、C3P0 等），以及 `java.lang.Runtime`、`ProcessBuilder` 等危险类和 `com.sun.`、`sun.` 等 JDK 内部包。拒绝列表不可被允许规则覆盖。如果确有需要，可通过 `DecodeFilter.getDefault().removeDenyPatterns(...)` 移除特定拒绝规则（需自行评估安全风险）。

**模式匹配规则**：
- **前缀匹配**（以`.`结尾）：匹配该包及其所有子包下的类。例如 `com.example.` 匹配 `com.example.Foo`、`com.example.sub.Bar` 等。
- **包名匹配**（不以`.`结尾，且不是完整类名）：仅匹配该包下的直接类，不含子包。例如 `com.example` 匹配 `com.example.Foo`，但不匹配 `com.example.sub.Bar`。默认允许列表中的 `java.lang` 和 `java.net` 就是这种模式。
- **精确匹配**（完整类名）：仅匹配这一个类。例如 `org.myapp.dto.UserDTO` 仅匹配 `org.myapp.dto.UserDTO`。

> **提示**：如果你的自定义类分散在多个包中，推荐使用前缀匹配（以`.`结尾）最为方便。

也可以通过编程式 API 配置（非 Spring Boot 场景）：

```java
DecodeFilter filter = DecodeFilter.getDefault();
filter.addAllowPatterns("com.example.");
```

如果反序列化时被拒绝，会输出ERROR日志（包含被拒绝的类名和配置示例），并抛出异常。Kryo和JSON路径抛出`DecodeFilterException`；Java序列化路径抛出`InvalidClassException`（JDK内部行为）。

**注意**：
- JDK 动态代理类（如 `jdk.proxy1.$Proxy0`）不在默认允许列表中。如果缓存了代理对象（如 Spring AOP 代理），需要添加允许规则（如 `jdk.proxy.`）。
- `java.rmi.`、`javax.naming.`、`java.lang.reflect.`、`javax.script.`、`javax.management.` 等包在内置拒绝列表中，添加允许规则无法绕过。`java.io`、`java.beans`（除 `EventHandler` 外）等包不在允许列表中但也不在拒绝列表中，可通过 `decodeFilterAllowPatterns` 或 `addAllowPatterns` 添加。
- 如果需要在内置拒绝列表之外额外屏蔽包或类，可通过 `decodeFilterDenyPatterns` 或 `addDenyPatterns` 添加。
