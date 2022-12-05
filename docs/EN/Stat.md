
A statistic monitor has installed for the ```Cache``` instance created by ```@CreateCache``` and ```@Cached``` when 
*jetcache.statIntervalMinutes* is greater than 0. The statistic data will be printed like below:
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
The *loadTime* is computed only in 2 scenarios:

* call ```computeIfAbsent``` in ```Cache``` 
* use ```@Cached``` annotation on method

The output of statistics can be customized:
```java
    // for 2.6+
    @Bean
    public Consumer<StatInfo> metricsCallback() {
        public Consumer<StatInfo> statCallback() {
            return new StatInfoLogger(false);
            // or implements another Consumer<StatInfo>
        }
    }

```
JetCache call ```statCallback``` method every *statIntervalMinutes* minutes. The default implementation is:
```java
return new StatInfoLogger(false);
```
There are more verbose information if the parameter of the constructor set to *true*.

With logback, you can use below configuration to output logs to a separate file:
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