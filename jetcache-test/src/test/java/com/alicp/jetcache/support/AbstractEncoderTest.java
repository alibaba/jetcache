package com.alicp.jetcache.support;

import com.alicp.jetcache.CacheValueHolder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;

/**
 * Created on 2016/10/8.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class AbstractEncoderTest {

    protected Function<Object, byte[]> encoder;
    protected Function<byte[], Object> decoder;

    protected void gcTest() {
        char[] cs = new char[500 * 1000];
        for (int i = 0; i < cs.length; i++) {
            cs[i] = 'a';
        }
        String largeString = new String(cs);

        for (int i = 0; i < 200; i++) {
            byte[] bytes = encoder.apply(largeString);
            Object result = decoder.apply(bytes);
            assertEquals(largeString, result);
        }
    }

    protected void baseTest() {
        assertEquals("123", decoder.apply(encoder.apply("123")));
        assertEquals(123, decoder.apply(encoder.apply(123)));
        Date date = new Date();
        assertEquals(date, decoder.apply(encoder.apply(date)));
        assertArrayEquals(new byte[]{1, 2, 3, -1}, (byte[]) decoder.apply(encoder.apply(new byte[]{1, 2, 3, -1})));
        assertNull(decoder.apply(encoder.apply(null)));
        testMap(new HashMap());
        testMap(new Hashtable());
        testMap(new ConcurrentHashMap());
//        testMap(Collections.synchronizedMap(new HashMap()));
        testList(new ArrayList());
        testList(new Vector());
        testList(new LinkedList());
        testSet(new HashSet());
        testQueue(new LinkedBlockingQueue<>());
//        testQueue(new ArrayBlockingQueue(10));
        testDeque(new LinkedBlockingDeque());

        TestObject q = new TestObject();
        q.setId(100);
        q.setEmail("aaa");
        q.setName("bbb");
        q.setData(new byte[]{1, 2, 3});
        Map<String, BigDecimal> m = new HashMap();
        m.put("12345", new BigDecimal(12345));

        byte bs[] = encoder.apply(q);
        TestObject q2 = (TestObject) decoder.apply(bs);

        compareTestObject(q, q2);


        bs = encoder.apply(new Object[]{q, 123});
        Object[] o = (Object[]) decoder.apply(bs);
        q2 = (TestObject) o[0];
        assertEquals(123, o[1]);

        compareTestObject(q, q2);

        A a = new A();
        a.setList(new ArrayList<>());
        a.getList().add(q);
        CacheValueHolder<CacheValueHolder<A>> h = new CacheValueHolder(
                new CacheValueHolder(a, 1000)
                , 1000);
        bs = encoder.apply(h);
        CacheValueHolder<CacheValueHolder<A>> h2 = (CacheValueHolder<CacheValueHolder<A>>) decoder.apply(bs);
        compareTestObject(h.getValue().getValue().getList().get(0)
                , h2.getValue().getValue().getList().get(0));

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

    private void testQueue(Queue q) {
        q.add(1);
        q.add(2);
        q.add(3);
        Queue q2 = (Queue) decoder.apply(encoder.apply(q));
        assertEquals(1, q2.poll());
        assertEquals(2, q2.poll());
        assertEquals(3, q2.poll());
    }

    private void testDeque(Deque q) {
        q.add(1);
        q.add(2);
        q.add(3);
        Deque q2 = (Deque) decoder.apply(encoder.apply(q));
        assertEquals(1, q2.pollFirst());
        assertEquals(3, q2.pollLast());
        assertEquals(2, q2.poll());
    }

    private void compareTestObject(TestObject q, TestObject q2) {
        assertEquals(q.getId(), q2.getId());
        assertEquals(q.getName(), q2.getName());
        assertEquals(q.getEmail(), q2.getEmail());
        assertArrayEquals((byte[]) q.getData(), (byte[]) q2.getData());
    }

    public static class A implements Serializable {
        private List<TestObject> list;

        public List<TestObject> getList() {
            return list;
        }

        public void setList(List<TestObject> list) {
            this.list = list;
        }
    }
}
