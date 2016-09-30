/**
 * Created on  13-09-26 16:28
 */
package com.alicp.jetcache.support;

import com.alicp.jetcache.CacheResultCode;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class DefaultCacheMonitor implements CacheMonitor {

    private ConcurrentHashMap<String, ConcurrentHashMap<String, Stat>> map
            = new ConcurrentHashMap();

    public void onGet(CacheAnnoConfig cacheAnnoConfig, String subArea, String key,
                      CacheResultCode localResult, CacheResultCode remoteResult) {
        ConcurrentHashMap<String, Stat> areaStat = map.get(cacheAnnoConfig.getArea());
        if (areaStat == null) {
            areaStat = new ConcurrentHashMap();
            map.put(cacheAnnoConfig.getArea(), areaStat);
        }
        Stat stat = areaStat.get(subArea);
        if (stat == null) {
            stat = new Stat();
            stat.area = cacheAnnoConfig.getArea();
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
        } else if(localResult == CacheResultCode.EXPIRED) {
            stat.localExpireCount++;
        }

        if (remoteResult != null) {
            stat.remoteGetCount++;
        }
        if (remoteResult == CacheResultCode.SUCCESS) {
            stat.remoteHitCount++;
        } else if (remoteResult == CacheResultCode.FAIL) {
            stat.remoteFailCount++;
        } else if(remoteResult == CacheResultCode.EXPIRED) {
            stat.remoteExpireCount++;
        }
    }

    public void onPut(CacheAnnoConfig cacheAnnoConfig, String subArea, String key,
                      Object value, CacheResultCode localResult, CacheResultCode remoteResult) {
    }

    public void warn(String message) {
    }

    public void error(String message) {
    }

    public StringBuilder getStatText() {
        StringBuilder sb = new StringBuilder(512);
        sb.append("-----------------------------------------------\n");
        sb.append("hit/get/rate\tlocalHit/localGet/localExpire/localFail\tremoteHit/remoteGet/remoteExpire/remoteFail\n");
        Set<Map.Entry<String, ConcurrentHashMap<String, Stat>>> entries = map.entrySet();
        for (Map.Entry<String, ConcurrentHashMap<String, Stat>> entry : entries) {
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
        DecimalFormat df = new DecimalFormat("#.0%");
        sb.append(stat.area).append('/').append(stat.subArea).append('\n');
        sb.append(stat.hitCount).append('/').append(stat.getCount).append('/').append(df.format(1.0 * stat.hitCount / stat.getCount));
        sb.append('\t');
        sb.append(stat.localHitCount).append('/').append(stat.localGetCount).append('/').append(stat.localExpireCount).append('/').append(stat.localFailCount);
        sb.append('\t');
        sb.append(stat.remoteHitCount).append('/').append(stat.remoteGetCount).append('/').append(stat.remoteExpireCount).append('/').append(stat.remoteFailCount);
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
        long localExpireCount;
        long remoteExpireCount;
        long localFailCount;
        long remoteFailCount;
    }
}

