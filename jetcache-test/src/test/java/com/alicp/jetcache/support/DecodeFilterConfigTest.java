package com.alicp.jetcache.support;

import com.alicp.jetcache.anno.support.ConfigProvider;
import com.alicp.jetcache.anno.support.GlobalCacheConfig;
import com.alicp.jetcache.autoconfigure.JetCacheProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for decode filter configuration via GlobalCacheConfig and ConfigProvider.
 */
public class DecodeFilterConfigTest {

    private DecodeFilter decodeFilter = DecodeFilter.getDefault();

    @AfterEach
    public void tearDown() {
        decodeFilter.reset();
    }

    @Test
    public void testProgrammaticApi() {
        decodeFilter.setEnabled(true);
        decodeFilter.addAllowPatterns("com.test.");
        assertTrue(decodeFilter.isAllowed("com.test.MyClass"));
        assertFalse(decodeFilter.isAllowed("com.other.MyClass"));
    }

    @Test
    public void testSetPatternsReplacesUserPatterns() {
        decodeFilter.clearAllowPatterns();
        decodeFilter.addAllowPatterns("com.a.");
        decodeFilter.addAllowPatterns("com.b.");
        assertTrue(decodeFilter.isAllowed("com.a.X"));
        assertTrue(decodeFilter.isAllowed("com.b.X"));
        assertFalse(decodeFilter.isAllowed("com.c.X"));
    }

    @Test
    public void testDisableFilter() {
        decodeFilter.setEnabled(false);
        assertTrue(decodeFilter.isAllowed("any.random.Class"));
    }

    @Test
    public void testGlobalCacheConfigDefaults() {
        GlobalCacheConfig config = new GlobalCacheConfig();
        assertTrue(config.isDecodeFilterEnabled(), "decodeFilterEnabled should default to true");
        assertNull(config.getDecodeFilterAllowPatterns(), "decodeFilterAllowPatterns should default to null");
        assertNull(config.getDecodeFilterDenyPatterns(), "decodeFilterDenyPatterns should default to null");
    }

    @Test
    public void testGlobalCacheConfigSetters() {
        GlobalCacheConfig config = new GlobalCacheConfig();
        config.setDecodeFilterEnabled(false);
        assertFalse(config.isDecodeFilterEnabled());

        config.setDecodeFilterAllowPatterns(List.of("com.example."));
        config.setDecodeFilterDenyPatterns(List.of("com.example.blocked."));
        assertEquals(List.of("com.example."), config.getDecodeFilterAllowPatterns());
        assertEquals(List.of("com.example.blocked."), config.getDecodeFilterDenyPatterns());
    }

    @Test
    public void testConfigProviderInitDecodeFilter() {
        GlobalCacheConfig config = new GlobalCacheConfig();
        config.setLocalCacheBuilders(new HashMap<>());
        config.setRemoteCacheBuilders(new HashMap<>());
        config.setDecodeFilterEnabled(true);
        config.setDecodeFilterAllowPatterns(List.of("com.cfgprovider."));
        config.setDecodeFilterDenyPatterns(List.of("com.cfgprovider.blocked."));

        ConfigProvider provider = new ConfigProvider();
        provider.setGlobalCacheConfig(config);
        provider.init();

        assertTrue(decodeFilter.isAllowed("com.cfgprovider.MyClass"));
        assertFalse(decodeFilter.isAllowed("com.cfgprovider.blocked.MyClass"));
        assertFalse(decodeFilter.isAllowed("com.other.MyClass"));

        provider.shutdown();
    }

    @Test
    public void testConfigProviderInitDecodeFilterDisabled() {
        GlobalCacheConfig config = new GlobalCacheConfig();
        config.setLocalCacheBuilders(new HashMap<>());
        config.setRemoteCacheBuilders(new HashMap<>());
        config.setDecodeFilterEnabled(false);

        ConfigProvider provider = new ConfigProvider();
        provider.setGlobalCacheConfig(config);
        provider.init();

        assertTrue(decodeFilter.isAllowed("com.anything.MyClass"));

        provider.shutdown();
    }

    @Test
    public void testJetCachePropertiesBindAllowAndDenyPatterns() {
        Map<String, String> source = new HashMap<>();
        source.put("jetcache.decodeFilterAllowPatterns[0]", "com.allow.");
        source.put("jetcache.decodeFilterDenyPatterns[0]", "com.allow.blocked.");

        JetCacheProperties props = new Binder(new MapConfigurationPropertySource(source))
                .bind("jetcache", Bindable.of(JetCacheProperties.class))
                .orElseGet(JetCacheProperties::new);

        assertEquals(List.of("com.allow."), props.getDecodeFilterAllowPatterns());
        assertEquals(List.of("com.allow.blocked."), props.getDecodeFilterDenyPatterns());
    }

}
