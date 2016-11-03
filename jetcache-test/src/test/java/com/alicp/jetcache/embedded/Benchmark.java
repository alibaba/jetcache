/**
 * Created on  13-10-19 23:40
 */
package com.alicp.jetcache.embedded;

import com.alicp.jetcache.anno.CacheConsts;
import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheGetResult;
import com.alicp.jetcache.CacheResultCode;
import org.junit.Ignore;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
@Ignore
public class Benchmark {

    private static final String FILE = "/Users/huangli/Downloads/input2.txt";

    private static final int THREAD_COUNT = 20;
    private static final int LINE_COUNT = 2733824;
    private static final int CACHE_LIMIT = 10000;

    private static final String VALUE = new String();

    public static void main(String[] args) throws Exception {
        String[][] data = readData();
        System.out.println("thead : " + THREAD_COUNT);
        System.out.println("data per thread : " + data[0].length);

        EmbeddedCacheBuilder builder = EmbeddedCacheBuilder.createEmbeddedCacheBuilder()
                .limit(CACHE_LIMIT);
        builder.buildFunc(c -> new LinkedHashMapCache((EmbeddedCacheConfig)c));
        Cache<String, String> cache = builder.build();

        CountDownLatch doneSignal = new CountDownLatch(THREAD_COUNT);

        System.out.println("start ...");
        T[] threads = new T[THREAD_COUNT];
        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i] = new T(data[i], cache, doneSignal);
            threads[i].setName("T" + i);
            threads[i].start();
        }

        doneSignal.await();
        long totalTime = 0;
        long hitCount = 0;
        long getCount = 0;
        for (int i = 0; i < THREAD_COUNT; i++) {
            T t = threads[i];
            long time = t.endTime - t.startTime;
            totalTime += time;
//            System.out.println("single thread:" + time + "ms");
            getCount += t.getCount;
            hitCount += t.hitCount;
        }

        System.out.println(totalTime);
        double rate = 1.0 * hitCount / getCount;
        System.out.println(rate);
        System.out.println(totalTime / rate);
    }

    static class T extends Thread {

        String[] data;
        Cache<String, String> cache;
        CountDownLatch doneSignal;

        volatile long startTime;
        volatile long endTime;

        volatile long getCount;
        volatile long hitCount;
        volatile long missCount;

        public T(String data[], Cache<String, String> cache, CountDownLatch doneSignal) {
            this.data = data;
            this.cache = cache;
            this.doneSignal = doneSignal;
        }

        @Override
        public void run() {
            startTime = System.currentTimeMillis();
            for (String userId : data) {
                getCount++;
                CacheGetResult result = cache.GET(userId);
                if (result.getResultCode() != CacheResultCode.SUCCESS) {
                    cache.PUT(userId, VALUE, CacheConsts.DEFAULT_EXPIRE, TimeUnit.SECONDS);
                    missCount++;
                } else {
                    hitCount++;
                }
            }
            endTime = System.currentTimeMillis();
            System.out.println(Thread.currentThread().getName() + " " + hitCount + "/" + missCount + "/" + getCount);
            doneSignal.countDown();
        }
    }

    public static String[][] readData() throws Exception {
        int rowsPerThread = LINE_COUNT / THREAD_COUNT;
        String[][] data = new String[THREAD_COUNT][rowsPerThread];
        FileInputStream fis = new FileInputStream(FILE);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis, "GBK"));
        String line;
        int threadIndex = 0;
        int rowIndex = 0;
        while ((line = br.readLine()) != null) {
            data[threadIndex++][rowIndex] = line;
            if (threadIndex >= THREAD_COUNT) {
                threadIndex = 0;
                rowIndex++;
                if (rowIndex >= rowsPerThread) {
                    break;
                }
            }
        }
        return data;
    }
}
