/**
 * Created on  13-09-17 11:26
 */
package com.alicp.jetcache.test.beans;

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.alicp.jetcache.test.support.DynamicQuery;
import com.alicp.jetcache.test.support.DynamicQueryWithEquals;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
@Component("testBean")
public class TestBean {

    private static int count = 0;


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
}
