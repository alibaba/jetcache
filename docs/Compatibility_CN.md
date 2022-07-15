# 2.7.0
* jetcahe-redis依赖jedis4，如果你使用spring data，它需要3，所以你需要自己把版本改回去，并且不能再使用jetcahe-redis了（改用jetcache-redis-springdata）
* 序列化工具kyro升级到5，它序列化的结果和4不兼容
* lettuce连接redis cluster需要在yml里面指定mode=cluster

# 2.6.0
* GET/GET_ALL方法不再触发自动刷新（大写的方法只简单访问缓存， 小写的方法才能触发这些附加功能）
* 不再支持lettuce4
* 不再支持jedis2.9
# 2.5.0
* 从2.3.3及更低版本升级到2.5.0会发生ClassCastException（如果你使用了MultiLevelCache或者cacheType.CacheType.BOTH）。
解决办法是先升级到2.4.4并且发布到生产环境，然后再升级到2.5.0。
* 子类的注解会覆盖接口和父类