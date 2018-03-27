/**
 * Created on 2018/3/27.
 */
package com.alicp.jetcache.anno.support;

import com.alicp.jetcache.CacheConfigException;
import com.alicp.jetcache.support.*;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class ConfigProviderTest {

    @Test
    public void parseQueryParameters() {
        assertEquals(0, ConfigProvider.parseQueryParameters(null).size());
        assertEquals("b", ConfigProvider.parseQueryParameters("a=b").get("a"));

        Map<String, String> m = ConfigProvider.parseQueryParameters("a=b&c=d");
        assertEquals("b", m.get("a"));
        assertEquals("d", m.get("c"));
        m = ConfigProvider.parseQueryParameters("a&b=");
        assertFalse(m.containsKey("a"));
        assertFalse(m.containsKey("b"));
    }

    @Test
    public void parseValueEncoder() {
        ConfigProvider cp = new ConfigProvider();
        AbstractValueEncoder encoder = (AbstractValueEncoder) cp.parseValueEncoder("kryo");
        assertEquals(KryoValueEncoder.class, encoder.getClass());
        assertTrue(encoder.isUseIdentityNumber());

        encoder = (AbstractValueEncoder) cp.parseValueEncoder("java?useIdentityNumber=false");
        assertEquals(JavaValueEncoder.class, encoder.getClass());
        assertFalse(encoder.isUseIdentityNumber());

        assertThrows(CacheConfigException.class, () -> cp.parseValueEncoder(null));
        assertThrows(CacheConfigException.class, () -> cp.parseValueEncoder("xxx"));
    }

    @Test
    public void parseValueDecoder() {
        ConfigProvider cp = new ConfigProvider();
        AbstractValueDecoder decoder = (AbstractValueDecoder) cp.parseValueDecoder("kryo");
        assertEquals(KryoValueDecoder.class, decoder.getClass());
        assertTrue(decoder.isUseIdentityNumber());

        decoder = (AbstractValueDecoder) cp.parseValueDecoder("java?useIdentityNumber=false");
        assertEquals(JavaValueDecoder.class, decoder.getClass());
        assertFalse(decoder.isUseIdentityNumber());

        assertThrows(CacheConfigException.class, () -> cp.parseValueDecoder(null));
        assertThrows(CacheConfigException.class, () -> cp.parseValueDecoder("xxx"));
    }
}
