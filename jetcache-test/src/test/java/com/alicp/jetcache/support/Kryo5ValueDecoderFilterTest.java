package com.alicp.jetcache.support;

import com.alicp.jetcache.CacheValueHolder;
import org.example.KryoFilterTestUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Kryo5ValueDecoderFilterTest {

    private final DecodeFilter decodeFilter = DecodeFilter.getDefault();

    @AfterEach
    public void tearDown() {
        decodeFilter.reset();
    }

    @Test
    public void testDefaultFilterAllowsJetCacheType() {
        CacheValueHolder<String> holder = new CacheValueHolder<>("test", 1000);
        byte[] bytes = Kryo5ValueEncoder.INSTANCE.apply(holder);
        Object result = Kryo5ValueDecoder.INSTANCE.apply(bytes);
        assertNotNull(result);
        assertEquals("test", ((CacheValueHolder<?>) result).getValue());
    }

    @Test
    public void testDefaultFilterRejectsCustomClass() {
        byte[] bytes = Kryo5ValueEncoder.INSTANCE.apply(new KryoFilterTestUser("test"));
        CacheEncodeException ex = assertThrows(CacheEncodeException.class, () -> Kryo5ValueDecoder.INSTANCE.apply(bytes));
        assertTrue(ex.getCause() instanceof DecodeFilterException);
        assertTrue(ex.getCause().getMessage().contains(KryoFilterTestUser.class.getName()));
    }

    @Test
    public void testUserPatternAllowsCustomClass() {
        decodeFilter.addAllowPatterns("org.example.");
        byte[] bytes = Kryo5ValueEncoder.INSTANCE.apply(new KryoFilterTestUser("test"));
        Object result = Kryo5ValueDecoder.INSTANCE.apply(bytes);
        KryoFilterTestUser user = assertInstanceOf(KryoFilterTestUser.class, result);
        assertEquals("test", user.getName());
    }

    @Test
    public void testFilterDisabledAllowsCustomClass() {
        decodeFilter.setEnabled(false);
        byte[] bytes = Kryo5ValueEncoder.INSTANCE.apply(new KryoFilterTestUser("test"));
        Object result = Kryo5ValueDecoder.INSTANCE.apply(bytes);
        KryoFilterTestUser user = assertInstanceOf(KryoFilterTestUser.class, result);
        assertEquals("test", user.getName());
    }

    @Test
    public void testUseIdentityNumberFalsePath() {
        decodeFilter.addAllowPatterns("org.example.");
        byte[] bytes = new Kryo5ValueEncoder(false, Kryo5ValueEncoder.DEFAULT_POOL).apply(new KryoFilterTestUser("test"));
        Object result = new Kryo5ValueDecoder(false, Kryo5ValueEncoder.DEFAULT_POOL).apply(bytes);
        KryoFilterTestUser user = assertInstanceOf(KryoFilterTestUser.class, result);
        assertEquals("test", user.getName());
    }

    @Test
    public void testFilterAppliedAcrossObjectPoolBorrows() {
        decodeFilter.addAllowPatterns("org.example.");
        byte[] bytes = Kryo5ValueEncoder.INSTANCE.apply(new KryoFilterTestUser("test"));
        KryoFilterTestUser allowed = assertInstanceOf(KryoFilterTestUser.class, Kryo5ValueDecoder.INSTANCE.apply(bytes));
        assertEquals("test", allowed.getName());

        decodeFilter.clearAllowPatterns();

        CacheEncodeException ex = assertThrows(CacheEncodeException.class, () -> Kryo5ValueDecoder.INSTANCE.apply(bytes));
        assertTrue(ex.getCause() instanceof DecodeFilterException);
        assertTrue(ex.getCause().getMessage().contains(KryoFilterTestUser.class.getName()));
    }
}
