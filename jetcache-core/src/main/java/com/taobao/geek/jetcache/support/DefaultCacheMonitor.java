/**
 * Created on  13-09-26 16:28
 */
package com.taobao.geek.jetcache.support;

import com.taobao.geek.jetcache.util.CopyOnWriteHashMap;

import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class DefaultCacheMonitor implements CacheMonitor {

    private CopyOnWriteHashMap<String, CopyOnWriteHashMap<String, Stat>> map
            = new CopyOnWriteHashMap<String, CopyOnWriteHashMap<String, Stat>>();

    @Override
    public void onGet(CacheConfig cacheConfig, String subArea, String key,
                      CacheResultCode localResult, CacheResultCode remoteResult) {
        CopyOnWriteHashMap<String, Stat> areaStat = map.get(cacheConfig.getArea());
        if (areaStat == null) {
            areaStat = new CopyOnWriteHashMap<String, Stat>();
            map.put(cacheConfig.getArea(), areaStat);
        }
        Stat stat = areaStat.get(subArea);
        if (stat == null) {
            stat = new Stat();
            stat.area = cacheConfig.getArea();
            stat.subArea = subArea;
            areaStat.put(subArea, stat);
        }
        stat.getCount++;

        if (localResult == CacheResultCode.SUCCESS || remoteResult == CacheResultCode.SUCCESS) {
            stat.hitCount++;
        }

        if (localResult != null) {
            stat.localGetCount++;
        }
        if (localResult == CacheResultCode.SUCCESS) {
            stat.localHitCount++;
        } else if (localResult == CacheResultCode.FAIL) {
            stat.localFailCount++;
        }

        if (remoteResult != null) {
            stat.remoteGetCount++;
        }
        if (remoteResult == CacheResultCode.SUCCESS) {
            stat.remoteHitCount++;
        } else if (remoteResult == CacheResultCode.FAIL) {
            stat.remoteFailCount++;
        }
    }

    @Override
    public void onPut(CacheConfig cacheConfig, String subArea, String key,
                      Object value, CacheResultCode localResult, CacheResultCode remoteResult) {
    }

    @Override
    public void warn(String message) {
    }

    @Override
    public void error(String message) {
    }

    public StringBuilder getStatText() {
        StringBuilder sb = new StringBuilder(512);
        sb.append("-----------------------------------------------\n");
        sb.append("hit/get\tlocal hit/local get/local fail\tremote hit/remote get/remote fail\n");
        Set<Map.Entry<String, CopyOnWriteHashMap<String, Stat>>> entries = map.entrySet();
        for (Map.Entry<String, CopyOnWriteHashMap<String, Stat>> entry : entries) {
            Set<Map.Entry<String, Stat>> areaMapEntries = entry.getValue().entrySet();
            for (Map.Entry<String, Stat> areaMapEntry : areaMapEntries) {
                Stat stat = areaMapEntry.getValue();
                append(sb, stat);
            }
        }
        sb.append("-----------------------------------------------\n");
        return sb;
    }

    private void append(StringBuilder sb, Stat stat) {
        sb.append(stat.area).append('/').append(stat.subArea).append('\n');
        sb.append(stat.hitCount).append('/').append(stat.getCount);
        sb.append('\t');
        sb.append(stat.localHitCount).append('/').append(stat.localGetCount).append('/').append(stat.localFailCount);
        sb.append('\t');
        sb.append(stat.remoteHitCount).append('/').append(stat.remoteGetCount).append('/').append(stat.remoteFailCount);
        sb.append('\n');
    }


    private static class Stat {
        String area;
        String subArea;
        long getCount;
        long hitCount;
        long localGetCount;
        long remoteGetCount;
        long localHitCount;
        long remoteHitCount;
        long localFailCount;
        long remoteFailCount;
    }
}

