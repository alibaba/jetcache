package com.alicp.jetcache.support;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JsonDecoderFilterTest {

    private DecodeFilter decodeFilter = DecodeFilter.getDefault();

    @AfterEach
    public void tearDown() {
        decodeFilter.reset();
    }

    static class TestJsonDecoder extends AbstractJsonDecoder {
        public TestJsonDecoder() {
            super(false);
        }

        @Override
        protected Object parseObject(byte[] buffer, int index, int len, Class clazz) {
            if (clazz == String.class) {
                return "";
            }
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                return null;
            }
        }
    }

    private byte[] buildDecodeBuffer(String className) {
        byte[] classNameBytes = className.getBytes(StandardCharsets.UTF_8);
        int totalLen = 2 + 2 + classNameBytes.length + 4;
        byte[] buf = new byte[totalLen];
        int index = 0;

        buf[index++] = 0;
        buf[index++] = 1;

        buf[index++] = (byte) ((classNameBytes.length >> 8) & 0xFF);
        buf[index++] = (byte) (classNameBytes.length & 0xFF);

        System.arraycopy(classNameBytes, 0, buf, index, classNameBytes.length);
        index += classNameBytes.length;

        buf[index++] = 0;
        buf[index++] = 0;
        buf[index++] = 0;
        buf[index++] = 0;

        return buf;
    }

    private byte[] buildNullObjectBuffer() {
        return new byte[]{(byte) 0xFF, (byte) 0xFF};
    }

    @Test
    public void testDefaultFilterAllowsJavaLangString() {
        TestJsonDecoder decoder = new TestJsonDecoder();
        byte[] buf = buildDecodeBuffer("java.lang.String");
        Object result = decoder.apply(buf);
        assertNotNull(result);
    }

    @Test
    public void testDefaultFilterRejectsCustomClass() {
        TestJsonDecoder decoder = new TestJsonDecoder();
        byte[] buf = buildDecodeBuffer("com.example.User");
        CacheEncodeException ex = assertThrows(CacheEncodeException.class, () -> {
            decoder.apply(buf);
        });
        assertTrue(ex.getCause() instanceof DecodeFilterException);
        assertTrue(ex.getCause().getMessage().contains("com.example.User"));
    }

    @Test
    public void testFilterDisabledAllowsAnyClass() {
        decodeFilter.setEnabled(false);
        TestJsonDecoder decoder = new TestJsonDecoder();
        byte[] buf = buildDecodeBuffer("org.example.KryoFilterTestUser");
        Object result = decoder.apply(buf);
        assertNotNull(result);
    }

    @Test
    public void testUserPatternAllowsCustomClass() {
        byte[] buf = buildDecodeBuffer("org.example.KryoFilterTestUser");
        TestJsonDecoder decoder = new TestJsonDecoder();
        CacheEncodeException ex = assertThrows(CacheEncodeException.class, () -> decoder.apply(buf));
        assertTrue(ex.getCause() instanceof DecodeFilterException);

        decodeFilter.addAllowPatterns("org.example.");
        Object result = decoder.apply(buf);
        assertNotNull(result);
    }

    @Test
    public void testNullObjectReturnsNullWithoutFilterCheck() {
        TestJsonDecoder decoder = new TestJsonDecoder();
        byte[] buf = buildNullObjectBuffer();
        Object result = decoder.apply(buf);
        assertNull(result);
    }

    @Test
    public void testDefaultFilterBlocksRiskyJavaPackages() {
        TestJsonDecoder decoder = new TestJsonDecoder();
        byte[] buf = buildDecodeBuffer("java.rmi.server.UnicastRef");
        CacheEncodeException ex = assertThrows(CacheEncodeException.class, () -> decoder.apply(buf));
        assertTrue(ex.getCause() instanceof DecodeFilterException);
    }

    @Test
    public void testFastjson2NestedTypeBlockedByFilter() {
        String maliciousJson = "[{\"@type\":\"java.rmi.server.UnicastRef\"}]";
        byte[] buf = buildDecodeBufferWithJson("java.util.ArrayList", maliciousJson);
        Fastjson2ValueDecoder decoder = new Fastjson2ValueDecoder(false);
        CacheEncodeException ex = assertThrows(CacheEncodeException.class, () -> decoder.apply(buf));
        assertTrue(ex.getCause() instanceof DecodeFilterException);
    }

    @Test
    public void testFastjson2AllowedNestedType() {
        String safeJson = "[{\"@type\":\"com.alicp.jetcache.support.TestObject\",\"id\":100}]";
        byte[] buf = buildDecodeBufferWithJson("java.util.ArrayList", safeJson);
        Fastjson2ValueDecoder decoder = new Fastjson2ValueDecoder(false);
        Object result = decoder.apply(buf);
        assertInstanceOf(List.class, result);
        List<?> list = (List<?>) result;
        assertEquals(1, list.size());
        assertInstanceOf(TestObject.class, list.get(0));
        assertEquals(100, ((TestObject) list.get(0)).getId());
    }

    @Test
    public void testJackson3NestedTypeBlockedByFilter() {
        String maliciousJson = "[\"java.util.ArrayList\",[[\"java.rmi.server.UnicastRef\",{}]]]";
        byte[] buf = buildDecodeBufferWithJson("java.util.ArrayList", maliciousJson);
        Jackson3ValueDecoder decoder = new Jackson3ValueDecoder(false);
        CacheEncodeException ex = assertThrows(CacheEncodeException.class, () -> decoder.apply(buf));
        assertHasDecodeFilterException(ex);
    }

    @Test
    public void testJackson3AllowedNestedType() {
        String safeJson = "[\"java.util.ArrayList\",[[\"com.alicp.jetcache.support.TestObject\",{\"id\":100}]]]";
        byte[] buf = buildDecodeBufferWithJson("java.util.ArrayList", safeJson);
        Jackson3ValueDecoder decoder = new Jackson3ValueDecoder(false);
        Object result = decoder.apply(buf);
        assertInstanceOf(List.class, result);
        List<?> list = (List<?>) result;
        assertEquals(1, list.size());
        assertInstanceOf(TestObject.class, list.get(0));
        assertEquals(100, ((TestObject) list.get(0)).getId());
    }

    @Test
    public void testFastjson2RoundTripWithObjectField() {
        TestObject original = new TestObject();
        original.setId(1);
        original.setName("test");
        original.setData(new TestObject());

        Fastjson2ValueEncoder encoder = new Fastjson2ValueEncoder(false);
        Fastjson2ValueDecoder decoder = new Fastjson2ValueDecoder(false);
        byte[] bytes = encoder.apply(original);
        Object result = decoder.apply(bytes);

        assertInstanceOf(TestObject.class, result);
        TestObject decoded = (TestObject) result;
        assertEquals(1, decoded.getId());
        assertEquals("test", decoded.getName());
        assertInstanceOf(TestObject.class, decoded.getData());
    }

    @Test
    public void testJackson3RoundTripWithObjectField() {
        TestObject original = new TestObject();
        original.setId(1);
        original.setName("test");
        original.setData(new TestObject());

        Jackson3ValueEncoder encoder = new Jackson3ValueEncoder(false);
        Jackson3ValueDecoder decoder = new Jackson3ValueDecoder(false);
        byte[] bytes = encoder.apply(original);
        Object result = decoder.apply(bytes);

        assertInstanceOf(TestObject.class, result);
        TestObject decoded = (TestObject) result;
        assertEquals(1, decoded.getId());
        assertEquals("test", decoded.getName());
        assertInstanceOf(TestObject.class, decoded.getData());
    }

    @Test
    public void testFastjson2RoundTripListWithMixedElements() {
        List<Object> original = new ArrayList<>();
        original.add("hello");
        original.add(123);
        original.add(new TestObject());

        Fastjson2ValueEncoder encoder = new Fastjson2ValueEncoder(false);
        Fastjson2ValueDecoder decoder = new Fastjson2ValueDecoder(false);
        byte[] bytes = encoder.apply(original);
        Object result = decoder.apply(bytes);

        assertInstanceOf(List.class, result);
        List<?> list = (List<?>) result;
        assertEquals(3, list.size());
        assertEquals("hello", list.get(0));
        assertEquals(123, list.get(1));
        assertInstanceOf(TestObject.class, list.get(2));
    }

    @Test
    public void testJackson3RoundTripListWithMixedElements() {
        List<Object> original = new ArrayList<>();
        original.add("hello");
        original.add(123);
        original.add(new TestObject());

        Jackson3ValueEncoder encoder = new Jackson3ValueEncoder(false);
        Jackson3ValueDecoder decoder = new Jackson3ValueDecoder(false);
        byte[] bytes = encoder.apply(original);
        Object result = decoder.apply(bytes);

        assertInstanceOf(List.class, result);
        List<?> list = (List<?>) result;
        assertEquals(3, list.size());
        assertEquals("hello", list.get(0));
        assertEquals(123, list.get(1));
        assertInstanceOf(TestObject.class, list.get(2));
    }

    private byte[] buildDecodeBufferWithJson(String className, String json) {
        byte[] classNameBytes = className.getBytes(StandardCharsets.UTF_8);
        byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);
        int totalLen = 2 + 2 + classNameBytes.length + 4 + jsonBytes.length;
        byte[] buf = new byte[totalLen];
        int index = 0;

        buf[index++] = 0;
        buf[index++] = 1;

        buf[index++] = (byte) ((classNameBytes.length >> 8) & 0xFF);
        buf[index++] = (byte) (classNameBytes.length & 0xFF);

        System.arraycopy(classNameBytes, 0, buf, index, classNameBytes.length);
        index += classNameBytes.length;

        buf[index++] = (byte) ((jsonBytes.length >> 24) & 0xFF);
        buf[index++] = (byte) ((jsonBytes.length >> 16) & 0xFF);
        buf[index++] = (byte) ((jsonBytes.length >> 8) & 0xFF);
        buf[index++] = (byte) (jsonBytes.length & 0xFF);

        System.arraycopy(jsonBytes, 0, buf, index, jsonBytes.length);

        return buf;
    }

    private static void assertHasDecodeFilterException(Throwable ex) {
        Throwable cause = ex.getCause();
        while (cause != null) {
            if (cause instanceof DecodeFilterException) {
                return;
            }
            cause = cause.getCause();
        }
        fail("Expected DecodeFilterException in cause chain, but got: " + ex.getCause());
    }
}
