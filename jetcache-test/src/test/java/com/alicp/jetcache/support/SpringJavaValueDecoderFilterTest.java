package com.alicp.jetcache.support;

import com.alicp.jetcache.CacheValueHolder;
import org.example.TestUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SpringJavaValueDecoderFilterTest {

    private DecodeFilter decodeFilter = DecodeFilter.getDefault();

    @AfterEach
    public void tearDown() {
        decodeFilter.reset();
    }

    @Test
    public void testDefaultFilterAllowsCacheValueHolder() {
        CacheValueHolder holder = new CacheValueHolder("test", -1);
        byte[] bytes = JavaValueEncoder.INSTANCE.apply(holder);
        Object result = SpringJavaValueDecoder.INSTANCE.apply(bytes);
        assertNotNull(result);
        assertEquals("test", ((CacheValueHolder) result).getValue());
    }

    @Test
    public void testDefaultFilterRejectsCustomClass() {
        TestUser user = new TestUser("test");
        byte[] bytes = JavaValueEncoder.INSTANCE.apply(user);
        CacheEncodeException ex = assertThrows(CacheEncodeException.class, () -> {
            SpringJavaValueDecoder.INSTANCE.apply(bytes);
        });
        assertNotNull(ex.getCause());
        assertTrue(ex.getCause() instanceof java.io.InvalidClassException);
        assertTrue(ex.getCause().getMessage().contains("REJECTED"));
    }

    @Test
    public void testFilterDisabledAllowsCustomClass() {
        decodeFilter.setEnabled(false);
        TestUser user = new TestUser("test");
        byte[] bytes = JavaValueEncoder.INSTANCE.apply(user);
        Object result = SpringJavaValueDecoder.INSTANCE.apply(bytes);
        assertNotNull(result);
        assertEquals("test", ((TestUser) result).getName());
    }

    @Test
    public void testUserPatternAllowsCustomClass() {
        decodeFilter.addAllowPatterns("org.example.");
        TestUser user = new TestUser("test");
        byte[] bytes = JavaValueEncoder.INSTANCE.apply(user);
        Object result = SpringJavaValueDecoder.INSTANCE.apply(bytes);
        assertNotNull(result);
        assertEquals("test", ((TestUser) result).getName());
    }

    @Test
    public void testUseIdentityNumberFalsePath() {
        CacheValueHolder holder = new CacheValueHolder("test", -1);
        byte[] bytes = new JavaValueEncoder(false).apply(holder);
        Object result = new SpringJavaValueDecoder(false).apply(bytes);
        assertNotNull(result);
        assertEquals("test", ((CacheValueHolder) result).getValue());
    }
}
