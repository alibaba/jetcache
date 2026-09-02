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

public class KryoValueDecoderFilterTest {

    private final DecodeFilter decodeFilter = DecodeFilter.getDefault();

    @AfterEach
    public void tearDown() {
        decodeFilter.reset();
    }

    @Test
    public void testDefaultFilterAllowsJetCacheType() {
        CacheValueHolder<String> holder = new CacheValueHolder<>("test", 1000);
        byte[] bytes = KryoValueEncoder.INSTANCE.apply(holder);
        Object result = KryoValueDecoder.INSTANCE.apply(bytes);
        assertNotNull(result);
        assertEquals("test", ((CacheValueHolder<?>) result).getValue());
    }

    @Test
    public void testDefaultFilterRejectsCustomClass() {
        byte[] bytes = KryoValueEncoder.INSTANCE.apply(new KryoFilterTestUser("test"));
        CacheEncodeException ex = assertThrows(CacheEncodeException.class, () -> KryoValueDecoder.INSTANCE.apply(bytes));
        assertTrue(ex.getCause() instanceof DecodeFilterException);
        assertTrue(ex.getCause().getMessage().contains(KryoFilterTestUser.class.getName()));
    }

    @Test
    public void testUserPatternAllowsCustomClass() {
        decodeFilter.addAllowPatterns("org.example.");
        byte[] bytes = KryoValueEncoder.INSTANCE.apply(new KryoFilterTestUser("test"));
        Object result = KryoValueDecoder.INSTANCE.apply(bytes);
        KryoFilterTestUser user = assertInstanceOf(KryoFilterTestUser.class, result);
        assertEquals("test", user.getName());
    }

    @Test
    public void testFilterDisabledAllowsCustomClass() {
        decodeFilter.setEnabled(false);
        byte[] bytes = KryoValueEncoder.INSTANCE.apply(new KryoFilterTestUser("test"));
        Object result = KryoValueDecoder.INSTANCE.apply(bytes);
        KryoFilterTestUser user = assertInstanceOf(KryoFilterTestUser.class, result);
        assertEquals("test", user.getName());
    }

    @Test
    public void testUseIdentityNumberFalsePath() {
        decodeFilter.addAllowPatterns("org.example.");
        byte[] bytes = new KryoValueEncoder(false, KryoValueEncoder.DEFAULT_POOL).apply(new KryoFilterTestUser("test"));
        Object result = new KryoValueDecoder(false, KryoValueEncoder.DEFAULT_POOL).apply(bytes);
        KryoFilterTestUser user = assertInstanceOf(KryoFilterTestUser.class, result);
        assertEquals("test", user.getName());
    }

    @Test
    public void testFilterAppliedAcrossObjectPoolBorrows() {
        decodeFilter.addAllowPatterns("org.example.");
        byte[] bytes = KryoValueEncoder.INSTANCE.apply(new KryoFilterTestUser("test"));
        KryoFilterTestUser allowed = assertInstanceOf(KryoFilterTestUser.class, KryoValueDecoder.INSTANCE.apply(bytes));
        assertEquals("test", allowed.getName());

        decodeFilter.clearAllowPatterns();

        CacheEncodeException ex = assertThrows(CacheEncodeException.class, () -> KryoValueDecoder.INSTANCE.apply(bytes));
        assertTrue(ex.getCause() instanceof DecodeFilterException);
        assertTrue(ex.getCause().getMessage().contains(KryoFilterTestUser.class.getName()));
    }
}
