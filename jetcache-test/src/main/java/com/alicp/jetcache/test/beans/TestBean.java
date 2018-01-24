/**
 * Created on  13-09-17 11:26
 */
package com.alicp.jetcache.test.beans;

import com.alicp.jetcache.anno.CacheInvalidate;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CacheUpdate;
import com.alicp.jetcache.anno.Cached;
import com.alicp.jetcache.test.support.DynamicQuery;
import com.alicp.jetcache.test.support.DynamicQueryWithEquals;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
@Component("testBean")
public class TestBean {

    private static int count = 0;
    Map<String, Integer> m = new HashMap<>();


    public TestBean() {
    }

    public int noCacheCount(){
        return count++;
    }

    @Cached
    public static int staticCount() {
        return count;
    }

    @Cached
    public int count() {
        return count++;
    }

    @Cached(expire = 50, cacheType = CacheType.LOCAL, timeUnit = TimeUnit.MILLISECONDS)
    public int countWithExpire50() {
        return count++;
    }

    @Cached
    public int count1() {
        return count++;
    }

    @Cached(cacheType = CacheType.LOCAL)
    public int countWithLocalCache(){
        return count++;
    }

    @Cached(cacheType = CacheType.BOTH)
    public int countWithBoth(){
        return count++;
    }


    @Cached(enabled = false)
    public int countWithDisabledCache(){
        return count++;
    }

    @Cached(area = "A1" , cacheType = CacheType.LOCAL)
    public int countLocalWithDynamicQuery(DynamicQuery q) {
        return count++;
    }

    @Cached(area = "A1" , cacheType = CacheType.LOCAL, keyConvertor = "fastjson")
    public int countLocalWithDynamicQueryAndKeyConvertor(DynamicQuery q) {
        return count++;
    }

    @Cached(area = "A1")
    public int countRemoteWithDynamicQuery(DynamicQuery q) {
        return count++;
    }

    @Cached(area = "A1")
    public int countLocalWithDynamicQueryWithEquals(DynamicQueryWithEquals q) {
        return count++;
    }

    @Cached(condition = "mvel{bean('configBean').trueProp}")
    public int countEnabledWithConfigBean(){
        return count++;
    }

    @Cached(condition = "mvel{bean('configBean').falseProp}")
    public int countDisabledWithConfigBean(){
        return count++;
    }

    @Cached(condition = "mvel{xxx('configBean').trueProp}")
    public int countWithWrongCondition(){
        return count++;
    }

    @Cached(condition = "mvel{args[0]}")
    public int count(boolean useCache){
        return count++;
    }

    @Cached(name="n1")
    public int namedCount1_WithNameN1(){
        return count++;
    }

    @Cached(name="n1")
    public int namedCount2_WithNameN1(){
        return count++;
    }

    @Cached(name="n2")
    public int namedCount_WithNameN2(){
        return count++;
    }


    @Cached(name = "c1", key = "args[0]")
    public int count(String id) {
        Integer v = m.get(id);
        if (v == null) {
            v = count++;
        }
        v++;
        m.put(id, v);
        return v;
    }

    @CacheUpdate(name = "c1", key = "#id", value = "args[1]")
    public void update(String id, int value) {
        m.put(id, value);
    }
    @CacheInvalidate(name = "c1", key = "args[0]")
    public void delete(String id) {
        m.remove(id);
    }

    @CacheUpdate(name = "c2", key = "args[0]", value = "args[1]")
    public void update2(String id, int value) {
        m.put(id, value);
    }

    @CacheInvalidate(name = "c2", key = "args[0]")
    public void delete2(String id) {
        m.remove(id);
    }
}
