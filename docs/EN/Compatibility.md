# 2.6.0
* GET/GET_ALL method of RefreshCache will not trigger auto refresh
* lettuce 4 is not supported
* jedis 2.9 is not supported
# 2.5.0
* ClassCastException may occurs when upgrade directly from versions <=2.3.3 and MultiLevelCache(or cacheType=CacheType.BOTH) is used. To solve this problem, upgrade to 2.4.4 and deploy it to product env first, then upgrade to 2.5.0 or above.
* Annotations on sub classes will override annotations on interfaces and super class.