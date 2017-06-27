package com.alicp.jetcache.test.external;

import com.alicp.jetcache.test.AbstractCacheTest;
import com.alicp.jetcache.test.support.DynamicQuery;
import com.alicp.jetcache.test.support.DynamicQueryWithEquals;
import org.junit.Assert;

/**
 * Created on 2016/12/30.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class AbstractExternalCacheTest extends AbstractCacheTest {
    protected void nullKeyConvertorTest() {
        {
            DynamicQuery d1 = new DynamicQuery();
            DynamicQuery d2 = new DynamicQuery();
            DynamicQuery d3 = new DynamicQuery();
            d1.setId(100);
            d2.setId(100);
            d3.setId(100);
            d1.setName("HL");
            d2.setName("HL");

            cache.put(d1, "V1");
            Assert.assertEquals("V1", cache.get(d2));
            Assert.assertNull(cache.get(d3));
        }

        {
            DynamicQueryWithEquals d1 = new DynamicQueryWithEquals();
            DynamicQueryWithEquals d2 = new DynamicQueryWithEquals();
            DynamicQueryWithEquals d3 = new DynamicQueryWithEquals();
            d1.setId(100);
            d2.setId(100);
            d3.setId(101);
            d1.setName("HL");
            d2.setName("HL2");

            cache.put(d1, "V2");
            Assert.assertNull("V2", cache.get(d2));
            Assert.assertNull(cache.get(d3));
        }
    }
}
