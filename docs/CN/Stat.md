
当yml中的jetcache.statIntervalMinutes大于0时，通过@CreateCache和@Cached配置出来的Cache自带监控。JetCache会按指定的时间定期通过logger输出统计信息。默认输出信息类似如下：
```
2017-01-12 19:00:00,001 INFO  support.StatInfoLogger - jetcache stat from 2017-01-12 18:59:00,000 to 2017-01-12 19:00:00,000
cache                                                |       qps|   rate|           get|           hit|          fail|        expire|avgLoadTime|maxLoadTime
-----------------------------------------------------+----------+-------+--------------+--------------+--------------+--------------+-----------+-----------
default_AlicpAppChannelManager.getAlicpAppChannelById|      0.00|  0.00%|             0|             0|             0|             0|        0.0|          0
default_ChannelManager.getChannelByAccessToten       |     30.02| 99.78%|         1,801|         1,797|             0|             4|        0.0|          0
default_ChannelManager.getChannelByAppChannelId      |      8.30| 99.60%|           498|           496|             0|             1|        0.0|          0
default_ChannelManager.getChannelById                |      6.65| 98.75%|           399|           394|             0|             4|        0.0|          0
default_ConfigManager.getChannelConfig               |      1.97| 96.61%|           118|           114|             0|             4|        0.0|          0
default_ConfigManager.getGameConfig                  |      0.00|  0.00%|             0|             0|             0|             0|        0.0|          0
default_ConfigManager.getInstanceConfig              |     43.98| 99.96%|         2,639|         2,638|             0|             0|        0.0|          0
default_ConfigManager.getInstanceConfigSettingMap    |      2.45| 70.75%|           147|           104|             0|            43|        0.0|          0
default_GameManager.getGameById                      |      1.33|100.00%|            80|            80|             0|             0|        0.0|          0
default_GameManager.getGameUrlByUrlKey               |      7.33|100.00%|           440|           440|             0|             0|        0.0|          0
default_InstanceManager.getInstanceById              |     30.98| 99.52%|         1,859|         1,850|             0|             0|        0.0|          0
default_InstanceManager.getInstanceById_local        |     30.98| 96.40%|         1,859|         1,792|             0|            67|        0.0|          0
default_InstanceManager.getInstanceById_remote       |      1.12| 86.57%|            67|            58|             0|             6|        0.0|          0
default_IssueDao.getIssueById                        |      7.62| 81.40%|           457|           372|             0|            63|        0.0|          0
default_IssueDao.getRecentOnSaleIssues               |      8.00| 85.21%|           480|           409|             0|            71|        0.0|          0
default_IssueDao.getRecentOpenAwardIssues            |      2.52| 82.78%|           151|           125|             0|            26|        0.0|          0
default_PrizeManager.getPrizeMap                     |      0.82|100.00%|            49|            49|             0|             0|        0.0|          0
default_TopicManager.getOnSaleTopics                 |      0.97|100.00%|            58|            58|             0|             0|        0.0|          0
default_TopicManager.getOnSaleTopics_local           |      0.97| 91.38%|            58|            53|             0|             5|        0.0|          0
default_TopicManager.getOnSaleTopics_remote          |      0.08|100.00%|             5|             5|             0|             0|        0.0|          0
default_TopicManager.getTopicByTopicId               |      2.90| 98.85%|           174|           172|             0|             0|        0.0|          0
default_TopicManager.getTopicByTopicId_local         |      2.90| 96.55%|           174|           168|             0|             6|        0.0|          0
default_TopicManager.getTopicByTopicId_remote        |      0.10| 66.67%|             6|             4|             0|             2|        0.0|          0
default_TopicManager.getTopicList                    |      0.02|100.00%|             1|             1|             0|             0|        0.0|          0
default_TopicManager.getTopicList_local              |      0.02|  0.00%|             1|             0|             0|             1|        0.0|          0
default_TopicManager.getTopicList_remote             |      0.02|100.00%|             1|             1|             0|             0|        0.0|          0
-----------------------------------------------------+----------+-------+--------------+--------------+--------------+--------------+-----------+-----------
```
只有使用computeIfAbsent方法或者@Cached注解才会统计loadTime。用get方法取缓存，没有命中的话自己去数据库load，显然是无法统计到的。

如果需要定制输出，可以这样做:
```java
    // for 2.6+
    @Bean
    public Consumer<StatInfo> metricsCallback() {
        public Consumer<StatInfo> statCallback() {
            return new StatInfoLogger(false);
            // ... 或实现自己的Consumer<StatInfo>
        }
    }

```

JetCache按statIntervalMinutes指定的周期，定期调用statCallback返回着这个Consumer，传入的StatInfo是已经统计好的数据。这个方法默认的实现是：
```java
return new StatInfoLogger(false);
```
StatInfoLogger的构造参数设置为true会有更详细的统计信息，包括put等操作的统计。StatInfoLogger输出的是给人读的信息，你也可以自定义logger将日志输出成特定格式，然后通过日志系统统一收集和统计。

如果想要让jetcache的日志输出到独立的文件中，在使用logback的情况下可以这样配置：
```xml
<appender name="JETCACHE_LOGFILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>jetcache.log</file>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
        <fileNamePattern>jetcache.log.%d{yyyy-MM-dd}</fileNamePattern>
        <maxHistory>30</maxHistory>
    </rollingPolicy>

    <encoder>
        <pattern>%-4relative [%thread] %-5level %logger{35} - %msg%n</pattern>
    </encoder>
</appender>

<logger name="com.alicp.jetcache" level="INFO" additivity="false">
    <appender-ref ref="JETCACHE_LOGFILE" />
</logger>
```