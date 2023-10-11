package com.alicp.jetcache.support;

import com.alicp.jetcache.CacheValueHolder;
import com.alicp.jetcache.VirtualThreadUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created on 2016/10/8.
 *
 * @author huangli
 */
public class AbstractEncoderTest {

    protected Function<Object, byte[]> encoder;
    protected Function<byte[], Object> decoder;

    protected void gcTest() {
        char[] cs = new char[500 * 1000];
        Arrays.fill(cs, 'a');
        String largeString = new String(cs);

        for (int i = 0; i < 200; i++) {
            byte[] bytes = encoder.apply(largeString);
            Object result = decoder.apply(bytes);
            assertEquals(largeString, result);
        }
    }

    protected void baseTest() {
        assertNull(decoder.apply(encoder.apply(null)));
        assertEquals("", decoder.apply(encoder.apply("")));
        assertEquals("123", decoder.apply(encoder.apply("123")));
        assertEquals(123, decoder.apply(encoder.apply(123)));
        Date date = new Date();
        assertEquals(date, decoder.apply(encoder.apply(date)));
        assertArrayEquals(new byte[]{1, 2, 3, -1}, (byte[]) decoder.apply(encoder.apply(new byte[]{1, 2, 3, -1})));
        testMap(new HashMap());
        testList(new ArrayList());
        testSet(new HashSet<>());

        TestObject q = new TestObject();
        q.setId(100);
        q.setEmail("aaa");
        q.setName("bbb");
        byte bs[] = encoder.apply(q);
        TestObject q2 = (TestObject) decoder.apply(bs);

        compareTestObject(q, q2);

        CacheValueHolder h = new CacheValueHolder(null, 10);
        CacheValueHolder h2 = (CacheValueHolder) decoder.apply(encoder.apply(h));
        assertEquals(h.getExpireTime(), h2.getExpireTime());
        assertNull(h2.getValue());

        h.setValue(q);
        h2 = (CacheValueHolder) decoder.apply(encoder.apply(h));
        assertEquals(h.getExpireTime(), h2.getExpireTime());
        compareTestObject((TestObject) h.getValue(), (TestObject) h2.getValue());

        CacheMessage cm = new CacheMessage();
        cm.setCacheName("c");
        CacheMessage cm2 = (CacheMessage) decoder.apply(encoder.apply(cm));
        assertEquals("c", cm2.getCacheName());
        assertNull(cm2.getKeys());

        cm.setKeys(new String[]{"12", "34"});
        cm2 = (CacheMessage) decoder.apply(encoder.apply(cm));
        assertEquals("12", cm2.getKeys()[0]);
        assertEquals("34", cm2.getKeys()[1]);
    }

    private void testMap(Map m) {
        m.put(1, "1");
        m.put(2, "2");
        m.put(3, "3");
        m.put(4, "4");
        m.put(5, "5");
        Map m2 = (Map) decoder.apply(encoder.apply(m));
        assertEquals("1", m2.get(1));
        assertEquals("2", m2.get(2));
        assertEquals("3", m2.get(3));
        assertEquals("4", m2.get(4));
        assertEquals("5", m2.get(5));
    }

    private void testList(List list) {
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        list.add(5);
        List list2 = (List) decoder.apply(encoder.apply(list));
        assertEquals(1, list2.get(0));
        assertEquals(2, list2.get(1));
        assertEquals(3, list2.get(2));
        assertEquals(4, list2.get(3));
        assertEquals(5, list2.get(4));
    }

    private void testSet(Set s) {
        s.add(1);
        s.add(2);
        s.add(3);
        s.add(4);
        s.add(5);
        Set s2 = (Set) decoder.apply(encoder.apply(s));
        assertTrue(s2.contains(1));
        assertTrue(s2.contains(2));
        assertTrue(s2.contains(3));
        assertTrue(s2.contains(4));
        assertTrue(s2.contains(5));
    }

    private void compareTestObject(TestObject q, TestObject q2) {
        assertEquals(q.getId(), q2.getId());
        assertEquals(q.getName(), q2.getName());
        assertEquals(q.getEmail(), q2.getEmail());
    }

    protected void writeHeader(byte[] buf, int header) {
        buf[0] = (byte) (header >> 24 & 0xFF);
        buf[1] = (byte) (header >> 16 & 0xFF);
        buf[2] = (byte) (header >> 8 & 0xFF);
        buf[3] = (byte) (header & 0xFF);
    }


    public void testByThreadPool(boolean isVirtual, int core, int size, Runnable runnable) throws InterruptedException {
        ExecutorService executorService = null;
        if(isVirtual) {
            executorService = VirtualThreadUtil.createExecuteor();
        }else if(core > 0) {
            executorService = Executors.newFixedThreadPool(core);
        }
        if(executorService == null) {
            return;
        }
        if(size <= 0) size = 100;
        for (int i = 0; i < size; i++) {
            executorService.submit(runnable);
        }
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);
    }

}
